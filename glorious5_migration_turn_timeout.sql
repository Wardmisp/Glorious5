-- ============================================================
-- MIGRATION : chrono de 15s par enchère (mode multijoueur en ligne)
-- ============================================================
-- À exécuter une fois dans le SQL Editor de Supabase, sur un projet où
-- glorious5_multiplayer_schema.sql a déjà été appliqué.
--
-- Sans danger à relancer plusieurs fois (add column if not exists,
-- create or replace function) : pas besoin d'effacer une exécution
-- précédente avant de recoller ce fichier.
--
-- Le contenu est identique à la section "6. CHRONO DE MISE" de
-- glorious5_multiplayer_schema.sql (gardé synchronisé avec ce fichier-ci) :
-- ce fichier existe séparément juste pour être recollé facilement sans
-- rouvrir tout le schéma.

-- 1) Colonne pour le chrono par enchère
alter table auctions add column if not exists turn_deadline timestamptz;

-- 2) present_next_player() : pose le chrono à la création d'une enchère
create or replace function present_next_player(p_match_id uuid)
returns uuid as $$
declare
  v_match record;
  v_p1_count integer;
  v_p2_count integer;
  v_p1_budget integer;
  v_p2_budget integer;
  v_next_player_id integer;
  v_target_user uuid;
  v_target_team_id uuid;
  v_new_auction_id uuid;
begin
  select * into v_match from matches where id = p_match_id for update;

  select count(*) into v_p1_count from match_team_players mtp
    join match_teams mt on mt.id = mtp.match_team_id
    where mt.match_id = p_match_id and mt.user_id = v_match.player1_id;
  select count(*) into v_p2_count from match_team_players mtp
    join match_teams mt on mt.id = mtp.match_team_id
    where mt.match_id = p_match_id and mt.user_id = v_match.player2_id;

  if v_p1_count >= v_match.team_size and v_p2_count >= v_match.team_size then
    perform compute_match_result(p_match_id);
    return null;
  end if;

  select id into v_next_player_id from "NbaBest1000"
  where id not in (
    select nba_player_id from match_team_players mtp
    join match_teams mt on mt.id = mtp.match_team_id
    where mt.match_id = p_match_id
  )
  order by random() limit 1;

  if v_next_player_id is null then
    update matches set status = 'completed', completed_at = now() where id = p_match_id;
    return null;
  end if;

  select budget_remaining into v_p1_budget from match_teams where match_id = p_match_id and user_id = v_match.player1_id;
  select budget_remaining into v_p2_budget from match_teams where match_id = p_match_id and user_id = v_match.player2_id;

  if v_p1_count >= v_match.team_size or v_p2_count >= v_match.team_size then
    v_target_user := case when v_p1_count >= v_match.team_size then v_match.player2_id else v_match.player1_id end;
    select id into v_target_team_id from match_teams where match_id = p_match_id and user_id = v_target_user;

    insert into auctions (match_id, nba_player_id, auction_type, status, winner_id, final_price, turn_user_id, completed_at)
    values (p_match_id, v_next_player_id, 'auto_assign', 'completed', v_target_user, 0, v_target_user, now())
    returning id into v_new_auction_id;

    insert into match_team_players (match_team_id, nba_player_id, price_paid)
    values (v_target_team_id, v_next_player_id, 0);

    return v_new_auction_id;
  end if;

  if v_p1_budget = 0 and v_p2_budget = 0 then
    v_target_user := coalesce(v_match.next_auto_assign_id, v_match.player1_id);
    select id into v_target_team_id from match_teams where match_id = p_match_id and user_id = v_target_user;

    insert into auctions (match_id, nba_player_id, auction_type, status, winner_id, final_price, turn_user_id, completed_at)
    values (p_match_id, v_next_player_id, 'auto_assign', 'completed', v_target_user, 0, v_target_user, now())
    returning id into v_new_auction_id;

    insert into match_team_players (match_team_id, nba_player_id, price_paid)
    values (v_target_team_id, v_next_player_id, 0);

    update matches set next_auto_assign_id =
      case when v_target_user = v_match.player1_id then v_match.player2_id else v_match.player1_id end
    where id = p_match_id;

    return v_new_auction_id;
  end if;

  if v_p1_budget = 0 or v_p2_budget = 0 then
    v_target_user := case when v_p1_budget = 0 then v_match.player2_id else v_match.player1_id end;
    select id into v_target_team_id from match_teams where match_id = p_match_id and user_id = v_target_user;

    insert into auctions (match_id, nba_player_id, auction_type, status, winner_id, final_price, turn_user_id, completed_at)
    values (p_match_id, v_next_player_id, 'auto_assign', 'completed', v_target_user, 0, v_target_user, now())
    returning id into v_new_auction_id;

    insert into match_team_players (match_team_id, nba_player_id, price_paid)
    values (v_target_team_id, v_next_player_id, 0);

    return v_new_auction_id;
  end if;

  -- CAS 4 : enchère normale — pas de chrono à la création (turn_deadline reste null).
  -- Il ne démarre que lorsque le joueur qui doit ouvrir est effectivement connecté et
  -- appelle start_turn_clock() lui-même (cf. plus bas) : sinon le délai pourrait déjà
  -- être bien entamé avant même que son écran ait fini de charger l'enchère.
  insert into auctions (match_id, nba_player_id, auction_type, status, turn_user_id)
  values (p_match_id, v_next_player_id, 'bid', 'active', coalesce(v_match.next_opener_id, v_match.player1_id))
  returning id into v_new_auction_id;

  update matches set next_opener_id =
    case when coalesce(v_match.next_opener_id, v_match.player1_id) = v_match.player1_id
    then v_match.player2_id else v_match.player1_id end
  where id = p_match_id;

  return v_new_auction_id;
end;
$$ language plpgsql security definer;

-- 3) handle_new_bid() : repousse le chrono à chaque nouvelle mise
create or replace function handle_new_bid()
returns trigger as $$
declare
  v_auction record;
  v_match record;
  v_other_user uuid;
begin
  select * into v_auction from auctions where id = new.auction_id for update;
  select * into v_match from matches where id = v_auction.match_id;

  select case when new.user_id = v_match.player1_id then v_match.player2_id else v_match.player1_id end
  into v_other_user;

  if new.amount is null then
    update auctions set status = 'completed', winner_id = v_auction.current_bidder_id,
      final_price = v_auction.current_bid, completed_at = now()
    where id = new.auction_id;

    update match_teams set budget_remaining = budget_remaining - v_auction.current_bid
    where match_id = v_auction.match_id and user_id = v_auction.current_bidder_id;

    insert into match_team_players (match_team_id, nba_player_id, price_paid)
    select id, v_auction.nba_player_id, v_auction.current_bid
    from match_teams where match_id = v_auction.match_id and user_id = v_auction.current_bidder_id;
  else
    update auctions set current_bid = new.amount, current_bidder_id = new.user_id,
      turn_user_id = v_other_user, turn_deadline = now() + interval '15 seconds'
    where id = new.auction_id;
  end if;

  return new;
end;
$$ language plpgsql security definer;

-- 4) Nouvelle RPC : force la résolution d'un tour en retard.
--    Appelable par n'importe lequel des deux joueurs — sans danger à
--    appeler "à l'aveugle" : ne fait rien si l'enchère n'est plus active
--    ou si le délai n'est pas (encore) dépassé.
create or replace function expire_turn_if_overdue(p_auction_id uuid)
returns void as $$
declare
  v_auction record;
  v_match record;
  v_winner uuid;
  v_winner_team_id uuid;
begin
  select * into v_auction from auctions where id = p_auction_id for update;
  if v_auction is null then
    return;
  end if;
  if v_auction.status != 'active' or v_auction.auction_type != 'bid' then
    return;
  end if;
  if v_auction.turn_deadline is null or now() < v_auction.turn_deadline then
    return;
  end if;

  select * into v_match from matches where id = v_auction.match_id;
  if auth.uid() not in (v_match.player1_id, v_match.player2_id) then
    raise exception 'Tu ne participes pas à ce match';
  end if;

  if v_auction.current_bidder_id is not null then
    -- Une mise est déjà en cours : le retardataire "passe", le meneur remporte.
    insert into bids (auction_id, user_id, amount)
    values (p_auction_id, v_auction.turn_user_id, null);
  else
    -- Personne n'a encore misé : le joueur qui devait ouvrir est attribué
    -- gratuitement à l'adversaire, directement.
    v_winner := case when v_auction.turn_user_id = v_match.player1_id then v_match.player2_id else v_match.player1_id end;
    select id into v_winner_team_id from match_teams where match_id = v_auction.match_id and user_id = v_winner;

    update auctions set status = 'completed', winner_id = v_winner, final_price = 0, completed_at = now()
    where id = p_auction_id;

    insert into match_team_players (match_team_id, nba_player_id, price_paid)
    values (v_winner_team_id, v_auction.nba_player_id, 0);
  end if;
end;
$$ language plpgsql security definer;

grant execute on function expire_turn_if_overdue(uuid) to authenticated;

-- 5) Démarre le chrono d'ouverture — appelée par le joueur dont c'est le tour de miser
--    (turn_user_id), lui-même, dès que son écran affiche cette enchère fraîchement créée
--    (turn_deadline encore null) et qu'il est prêt à miser. Une fois posé, turn_deadline
--    est un timestamp serveur unique lu par les deux clients : le décompte avance donc à
--    l'identique des deux côtés, sans action supplémentaire de l'adversaire. Sans effet si
--    le chrono est déjà lancé ou si une mise a déjà été posée (handle_new_bid() gère le
--    chrono dans ce cas).
create or replace function start_turn_clock(p_auction_id uuid)
returns void as $$
declare
  v_auction record;
begin
  select * into v_auction from auctions where id = p_auction_id for update;
  if v_auction is null then
    return;
  end if;
  if v_auction.status != 'active' or v_auction.auction_type != 'bid' then
    return;
  end if;
  if v_auction.turn_deadline is not null then
    return; -- déjà démarré
  end if;
  if v_auction.turn_user_id != auth.uid() then
    return; -- seul le joueur dont c'est le tour peut démarrer son propre chrono
  end if;

  update auctions set turn_deadline = now() + interval '15 seconds' where id = p_auction_id;
end;
$$ language plpgsql security definer;

grant execute on function start_turn_clock(uuid) to authenticated;

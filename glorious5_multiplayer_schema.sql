-- ============================================================
-- GLORIOUS 5 — Schéma multijoueur (match par enchères 1v1)
-- ============================================================
-- Règles implémentées :
-- - Enchère live, tour à tour, jusqu'à ce qu'un joueur passe
-- - Budget de 50 par équipe (défaut), mise 0 possible dès la 1ère mise
-- - Budget à 0 = exclu des enchères, récupère les joueurs
--   que l'adversaire ne remporte pas (gratuit)
-- - Équipe à 5 joueurs (team_size) = sort du système d'enchère,
--   les joueurs suivants sont attribués automatiquement à l'autre équipe
-- - Ouverture d'enchère : tour à tour simple (alterne à chaque joueur présenté)
-- - Si les 2 équipes sont à budget 0 en même temps : attribution
--   alternée entre les deux
-- - Attribution automatique affichée un par un côté client (pas de cascade
--   automatique en base : le client appelle present_next_player() à chaque fois)

-- ============================================================
-- 0. PRÉREQUIS : corriger le schéma de NbaBest1000
-- ============================================================
-- Nécessaire AVANT de créer les tables ci-dessous, qui référencent
-- NbaBest1000(id) en clé étrangère et ses stats pour le calcul de match.

-- a) Clé primaire sur id (obligatoire pour les foreign keys plus bas).
--    Si cette ligne échoue : il y a des id NULL ou dupliqués dans ta
--    table à corriger avant de continuer (vérifie avec :
--    select id, count(*) from "NbaBest1000" group by id having count(*) > 1;)
alter table "NbaBest1000" add primary key (id);

-- b) stl, blk et fg3_pct sont en TEXT dans ta table (résidu de l'import
--    CSV) — on les convertit en colonnes numériques propres, nécessaires
--    pour les calculs de percentile plus bas. safe_numeric() renvoie
--    NULL plutôt que planter si une valeur n'est pas convertible.
create or replace function safe_numeric(p_input text) returns double precision as $$
begin
  return p_input::double precision;
exception when others then
  return null;
end;
$$ language plpgsql immutable;

alter table "NbaBest1000" add column stl_num double precision;
alter table "NbaBest1000" add column blk_num double precision;
alter table "NbaBest1000" add column fg3_pct_num double precision;

update "NbaBest1000" set
  stl_num = safe_numeric(stl),
  blk_num = safe_numeric(blk),
  fg3_pct_num = safe_numeric(fg3_pct);

-- c) Colonnes pour stocker les percentiles et le score final de chaque
--    joueur (calculés une seule fois ci-dessous, section 2bis — les
--    données NBA historiques ne changent pas, pas besoin de recalculer
--    à chaque match).
alter table "NbaBest1000" add column p_pts double precision;
alter table "NbaBest1000" add column p_reb double precision;
alter table "NbaBest1000" add column p_ast double precision;
alter table "NbaBest1000" add column p_stl double precision;
alter table "NbaBest1000" add column p_blk double precision;
alter table "NbaBest1000" add column p_fg_pct double precision;
alter table "NbaBest1000" add column p_fg3_pct double precision;
alter table "NbaBest1000" add column p_ft_pct double precision;
alter table "NbaBest1000" add column p_per double precision;
alter table "NbaBest1000" add column p_ws_game double precision;
alter table "NbaBest1000" add column eff double precision;
alter table "NbaBest1000" add column att double precision;
alter table "NbaBest1000" add column def_score double precision;
alter table "NbaBest1000" add column imp double precision;
alter table "NbaBest1000" add column score_total double precision;

-- d) Calcule le percentile de chaque stat pour chaque joueur, selon la
--    formule : (nb joueurs < valeur + 0.5 × nb joueurs = valeur) / total × 100
--    Fait sur ~1000 lignes, une seule fois : coût négligeable.
update "NbaBest1000" t set
  p_pts = (select (count(*) filter (where s.pts < t.pts) + 0.5*count(*) filter (where s.pts = t.pts))::double precision
           / nullif((select count(*) from "NbaBest1000"), 0) * 100 from "NbaBest1000" s),
  p_reb = (select (count(*) filter (where s.reb < t.reb) + 0.5*count(*) filter (where s.reb = t.reb))::double precision
           / nullif((select count(*) from "NbaBest1000"), 0) * 100 from "NbaBest1000" s),
  p_ast = (select (count(*) filter (where s.ast < t.ast) + 0.5*count(*) filter (where s.ast = t.ast))::double precision
           / nullif((select count(*) from "NbaBest1000"), 0) * 100 from "NbaBest1000" s),
  p_stl = (select (count(*) filter (where s.stl_num < t.stl_num) + 0.5*count(*) filter (where s.stl_num = t.stl_num))::double precision
           / nullif((select count(*) from "NbaBest1000"), 0) * 100 from "NbaBest1000" s),
  p_blk = (select (count(*) filter (where s.blk_num < t.blk_num) + 0.5*count(*) filter (where s.blk_num = t.blk_num))::double precision
           / nullif((select count(*) from "NbaBest1000"), 0) * 100 from "NbaBest1000" s),
  p_fg_pct = (select (count(*) filter (where s.fg_pct < t.fg_pct) + 0.5*count(*) filter (where s.fg_pct = t.fg_pct))::double precision
           / nullif((select count(*) from "NbaBest1000"), 0) * 100 from "NbaBest1000" s),
  p_fg3_pct = (select (count(*) filter (where s.fg3_pct_num < t.fg3_pct_num) + 0.5*count(*) filter (where s.fg3_pct_num = t.fg3_pct_num))::double precision
           / nullif((select count(*) from "NbaBest1000"), 0) * 100 from "NbaBest1000" s),
  p_ft_pct = (select (count(*) filter (where s.ft_pct < t.ft_pct) + 0.5*count(*) filter (where s.ft_pct = t.ft_pct))::double precision
           / nullif((select count(*) from "NbaBest1000"), 0) * 100 from "NbaBest1000" s),
  p_per = (select (count(*) filter (where s.per < t.per) + 0.5*count(*) filter (where s.per = t.per))::double precision
           / nullif((select count(*) from "NbaBest1000"), 0) * 100 from "NbaBest1000" s),
  p_ws_game = (select (count(*) filter (where (s.win_shares/nullif(s.games,0)) < (t.win_shares/nullif(t.games,0)))
                      + 0.5*count(*) filter (where (s.win_shares/nullif(s.games,0)) = (t.win_shares/nullif(t.games,0))))::double precision
           / nullif((select count(*) from "NbaBest1000"), 0) * 100 from "NbaBest1000" s);

-- e) Scores agrégés (EFF, DEF, IMP) puis score final du joueur, selon
--    le document match_calculation_logic.md
update "NbaBest1000" set
  eff = 0.40*p_fg_pct + 0.35*p_fg3_pct + 0.25*p_ft_pct,
  def_score = 0.50*p_stl + 0.50*p_blk,
  imp = 0.60*p_per + 0.40*p_ws_game;

update "NbaBest1000" set
  att = 0.60*p_pts + 0.40*eff; -- calculé pour info/affichage, non utilisé dans score_total (cf. doc)

update "NbaBest1000" set
  score_total = 0.20*p_pts + 0.10*p_reb + 0.15*p_ast + 0.10*p_stl + 0.10*p_blk + 0.10*eff + 0.25*imp;

-- ============================================================
-- 1. TABLES
-- ============================================================

create table matches (
  id uuid primary key default gen_random_uuid(),
  player1_id uuid references auth.users(id) not null,
  player2_id uuid references auth.users(id),
  status text not null default 'waiting'
    check (status in ('waiting', 'drafting', 'completed', 'cancelled')),
  team_size integer not null default 5,
  budget integer not null default 50,
  next_opener_id uuid references auth.users(id), -- qui ouvre la prochaine enchère
  next_auto_assign_id uuid references auth.users(id), -- alternance si double budget à 0
  winner_id uuid references auth.users(id),
  created_at timestamptz default now(),
  completed_at timestamptz
);

create table match_teams (
  id uuid primary key default gen_random_uuid(),
  match_id uuid references matches(id) not null,
  user_id uuid references auth.users(id) not null,
  budget_remaining integer not null default 50,
  total_score numeric,
  created_at timestamptz default now(),
  unique (match_id, user_id)
);

create table auctions (
  id uuid primary key default gen_random_uuid(),
  match_id uuid references matches(id) not null,
  nba_player_id integer references "NbaBest1000"(id) not null,
  auction_type text not null default 'bid'
    check (auction_type in ('bid', 'auto_assign')),
  status text not null default 'active'
    check (status in ('active', 'completed')),
  current_bid integer not null default 0,
  current_bidder_id uuid references auth.users(id),
  turn_user_id uuid references auth.users(id) not null,
  winner_id uuid references auth.users(id),
  final_price integer,
  created_at timestamptz default now(),
  completed_at timestamptz
);

create table bids (
  id uuid primary key default gen_random_uuid(),
  auction_id uuid references auctions(id) not null,
  user_id uuid references auth.users(id) not null,
  amount integer, -- null = "passe"
  created_at timestamptz default now()
);

create table match_team_players (
  id uuid primary key default gen_random_uuid(),
  match_team_id uuid references match_teams(id) not null,
  nba_player_id integer references "NbaBest1000"(id) not null,
  price_paid integer not null default 0,
  won_at timestamptz default now(),
  unique (match_team_id, nba_player_id)
);

-- ============================================================
-- 2. FONCTION : présente UN SEUL prochain joueur
--    (pas de récursion — le client rappelle cette fonction
--    à chaque fois qu'il est prêt à afficher le joueur suivant)
-- ============================================================

create or replace function present_next_player(p_match_id uuid)
returns uuid as $$ -- retourne l'id de l'auction créée, ou null si match terminé
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

  -- les deux équipes sont pleines → calcule le résultat et termine le match
  if v_p1_count >= v_match.team_size and v_p2_count >= v_match.team_size then
    perform compute_match_result(p_match_id);
    return null;
  end if;

  -- tire un joueur non encore attribué dans ce match
  select id into v_next_player_id from "NbaBest1000"
  where id not in (
    select nba_player_id from match_team_players mtp
    join match_teams mt on mt.id = mtp.match_team_id
    where mt.match_id = p_match_id
  )
  order by random() limit 1;

  if v_next_player_id is null then
    -- plus aucun joueur disponible dans le pool → fin du match
    update matches set status = 'completed', completed_at = now() where id = p_match_id;
    return null;
  end if;

  select budget_remaining into v_p1_budget from match_teams where match_id = p_match_id and user_id = v_match.player1_id;
  select budget_remaining into v_p2_budget from match_teams where match_id = p_match_id and user_id = v_match.player2_id;

  -- CAS 1 : une équipe est pleine → l'autre récupère automatiquement
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

  -- CAS 2 : les deux équipes sont à budget 0 → attribution alternée
  if v_p1_budget = 0 and v_p2_budget = 0 then
    v_target_user := coalesce(v_match.next_auto_assign_id, v_match.player1_id);
    select id into v_target_team_id from match_teams where match_id = p_match_id and user_id = v_target_user;

    insert into auctions (match_id, nba_player_id, auction_type, status, winner_id, final_price, turn_user_id, completed_at)
    values (p_match_id, v_next_player_id, 'auto_assign', 'completed', v_target_user, 0, v_target_user, now())
    returning id into v_new_auction_id;

    insert into match_team_players (match_team_id, nba_player_id, price_paid)
    values (v_target_team_id, v_next_player_id, 0);

    -- alterne le prochain bénéficiaire
    update matches set next_auto_assign_id =
      case when v_target_user = v_match.player1_id then v_match.player2_id else v_match.player1_id end
    where id = p_match_id;

    return v_new_auction_id;
  end if;

  -- CAS 3 : une seule équipe est à budget 0 → l'autre récupère automatiquement
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

  -- CAS 4 : enchère normale — l'ouverture alterne à chaque joueur présenté
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

-- ============================================================
-- 2bis. CALCUL DU RÉSULTAT — d'après match_calculation_logic.md
-- ============================================================
-- Hypothèse : le draft par enchères n'impose pas un joueur par poste
-- (une équipe peut finir avec 3 SG et 0 pivot). Les coefficients de
-- poste du document ne peuvent donc pas s'appliquer au poste réel de
-- chaque joueur sans risquer une équipe incomplète en coefficients.
-- À la place, les coefficients sont appliqués par RANG DE FORCE dans
-- l'équipe : le joueur au score_total le plus élevé reçoit 0.22, le
-- 2e et 3e 0.20, le 4e 0.20, le 5e 0.18 — l'esprit du document (le
-- meilleur joueur pèse le plus) est conservé sans dépendre du poste.

create or replace function calc_team_score(p_team_id uuid) returns numeric as $$
declare
  v_coeffs numeric[] := array[0.22, 0.20, 0.20, 0.20, 0.18];
  v_total numeric := 0;
  v_idx integer := 1;
  v_score double precision;
begin
  for v_score in
    select nb.score_total
    from match_team_players mtp
    join "NbaBest1000" nb on nb.id = mtp.nba_player_id
    where mtp.match_team_id = p_team_id
    order by nb.score_total desc
  loop
    v_total := v_total + coalesce(v_score, 0) * v_coeffs[least(v_idx, array_length(v_coeffs, 1))];
    v_idx := v_idx + 1;
  end loop;

  return v_total;
end;
$$ language plpgsql security definer;

-- Calcule les scores d'équipe, la probabilité de victoire (fonction
-- logistique, cf. doc section 3) et tire le gagnant au sort pondéré
-- (section 4 du doc). Appelée automatiquement par present_next_player
-- quand les deux équipes sont complètes — pas d'appel client direct.
create or replace function compute_match_result(p_match_id uuid)
returns void as $$
declare
  v_match record;
  v_p1_team_id uuid;
  v_p2_team_id uuid;
  v_p1_score numeric;
  v_p2_score numeric;
  v_diff numeric;
  v_p1_win_prob numeric;
  v_winner uuid;
begin
  select * into v_match from matches where id = p_match_id;

  select id into v_p1_team_id from match_teams where match_id = p_match_id and user_id = v_match.player1_id;
  select id into v_p2_team_id from match_teams where match_id = p_match_id and user_id = v_match.player2_id;

  v_p1_score := calc_team_score(v_p1_team_id);
  v_p2_score := calc_team_score(v_p2_team_id);

  update match_teams set total_score = v_p1_score where id = v_p1_team_id;
  update match_teams set total_score = v_p2_score where id = v_p2_team_id;

  -- fonction logistique : P(A gagne) = 1 / (1 + e^(-D/8))
  v_diff := v_p1_score - v_p2_score;
  v_p1_win_prob := 1.0 / (1.0 + exp(-v_diff / 8.0));

  -- tirage aléatoire pondéré (l'équipe la plus forte a plus de chances,
  -- mais l'upset reste possible)
  if random() < v_p1_win_prob then
    v_winner := v_match.player1_id;
  else
    v_winner := v_match.player2_id;
  end if;

  update matches set winner_id = v_winner, status = 'completed', completed_at = now()
  where id = p_match_id;
end;
$$ language plpgsql security definer;

-- ============================================================
-- 3. TRIGGER : traite chaque mise/passe sur une enchère active
-- ============================================================

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
    -- passe → clôture l'enchère au profit du dernier enchérisseur
    -- (impossible d'arriver ici sans enchérisseur, voir policy : 1ère action = mise obligatoire)
    update auctions set status = 'completed', winner_id = v_auction.current_bidder_id,
      final_price = v_auction.current_bid, completed_at = now()
    where id = new.auction_id;

    update match_teams set budget_remaining = budget_remaining - v_auction.current_bid
    where match_id = v_auction.match_id and user_id = v_auction.current_bidder_id;

    insert into match_team_players (match_team_id, nba_player_id, price_paid)
    select id, v_auction.nba_player_id, v_auction.current_bid
    from match_teams where match_id = v_auction.match_id and user_id = v_auction.current_bidder_id;
  else
    -- nouvelle mise valide → bascule le tour vers l'adversaire
    update auctions set current_bid = new.amount, current_bidder_id = new.user_id,
      turn_user_id = v_other_user
    where id = new.auction_id;
  end if;

  return new;
end;
$$ language plpgsql security definer;

create trigger on_bid_inserted
  after insert on bids
  for each row execute function handle_new_bid();

-- ============================================================
-- 4. ROW LEVEL SECURITY
-- ============================================================

alter table matches enable row level security;
alter table match_teams enable row level security;
alter table auctions enable row level security;
alter table bids enable row level security;
alter table match_team_players enable row level security;

-- Lecture publique (tout le monde voit l'état du match en cours)
create policy "read matches" on matches for select using (true);
create policy "read match_teams" on match_teams for select using (true);
create policy "read auctions" on auctions for select using (true);
create policy "read bids" on bids for select using (true);
create policy "read match_team_players" on match_team_players for select using (true);

-- Miser : uniquement à son tour, sur une enchère de type 'bid' active,
-- avec un budget > 0, et une 1ère mise obligatoire (pas de passe avant
-- qu'un premier montant n'ait été posé)
create policy "bid on your turn only" on bids
  for insert with check (
    exists (
      select 1 from auctions a
      join match_teams mt on mt.match_id = a.match_id and mt.user_id = auth.uid()
      where a.id = auction_id
      and a.turn_user_id = auth.uid()
      and a.status = 'active'
      and a.auction_type = 'bid'
      and mt.budget_remaining > 0
      and (
        (a.current_bidder_id is null and amount is not null and amount >= 0 and amount <= mt.budget_remaining)
        or (a.current_bidder_id is not null and (
          amount is null
          or (amount > a.current_bid and amount <= mt.budget_remaining)
        ))
      )
    )
  );

-- Note : matches, match_teams, auctions, match_team_players ne doivent
-- être écrits QUE par les fonctions security definer ci-dessus
-- (appelées via RPC), jamais directement par le client. Pas de policy
-- INSERT/UPDATE côté client sur ces tables = comportement voulu.

-- ============================================================
-- 5. RPC : créer un match et le rejoindre
-- ============================================================
-- Ce sont les DEUX SEULES fonctions à appeler pour démarrer un match.
-- Aucun insert direct depuis le client sur matches/match_teams n'est
-- possible (pas de policy pour ça, volontairement).

-- Crée un match. Si p_opponent_id est fourni → match privé, démarre
-- direct en 'drafting'. Sinon → match public en attente d'adversaire
-- (à afficher dans un lobby / liste de matchs à rejoindre).
create or replace function create_match(
  p_opponent_id uuid default null,
  p_budget integer default 50,
  p_team_size integer default 5
)
returns uuid as $$
declare
  v_match_id uuid;
begin
  if p_opponent_id = auth.uid() then
    raise exception 'Tu ne peux pas jouer contre toi-même';
  end if;

  insert into matches (player1_id, player2_id, status, budget, team_size)
  values (
    auth.uid(),
    p_opponent_id,
    case when p_opponent_id is null then 'waiting' else 'drafting' end,
    p_budget,
    p_team_size
  )
  returning id into v_match_id;

  insert into match_teams (match_id, user_id, budget_remaining)
  values (v_match_id, auth.uid(), p_budget);

  if p_opponent_id is not null then
    insert into match_teams (match_id, user_id, budget_remaining)
    values (v_match_id, p_opponent_id, p_budget);
  end if;

  return v_match_id;
end;
$$ language plpgsql security definer;

-- Rejoint un match public en attente (status = 'waiting', sans player2).
-- Passe le match en 'drafting' une fois les deux joueurs présents.
create or replace function join_match(p_match_id uuid)
returns void as $$
declare
  v_match record;
begin
  select * into v_match from matches where id = p_match_id for update;

  if v_match is null then
    raise exception 'Ce match n''existe pas';
  end if;
  if v_match.status != 'waiting' then
    raise exception 'Ce match n''est plus disponible';
  end if;
  if v_match.player2_id is not null then
    raise exception 'Ce match a déjà un adversaire';
  end if;
  if v_match.player1_id = auth.uid() then
    raise exception 'Tu ne peux pas rejoindre ton propre match';
  end if;

  update matches set player2_id = auth.uid(), status = 'drafting' where id = p_match_id;

  insert into match_teams (match_id, user_id, budget_remaining)
  values (p_match_id, auth.uid(), v_match.budget);
end;
$$ language plpgsql security definer;

-- Droits d'exécution : sans ça, l'app ne pourra appeler aucune de ces
-- fonctions RPC depuis le client (erreur "permission denied").
grant execute on function create_match(uuid, integer, integer) to authenticated;
grant execute on function join_match(uuid) to authenticated;
grant execute on function present_next_player(uuid) to authenticated;

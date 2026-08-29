-- ============================================================
-- MIGRATION : expiration des parties en attente (10 minutes)
-- ============================================================
-- À exécuter une fois dans le SQL Editor de Supabase, sur un projet où
-- glorious5_multiplayer_schema.sql a déjà été appliqué.
--
-- Le lobby (listOpenMatches côté app) filtre déjà les parties de plus
-- de 10 minutes sans adversaire. Ce fichier applique la même limite à
-- join_match() : sans ça, un ami pourrait toujours rejoindre une vieille
-- partie via un code copié il y a longtemps, alors qu'elle n'apparaît
-- plus nulle part dans le lobby — incohérent.
--
-- Sans danger à relancer plusieurs fois (create or replace function).

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
  if v_match.created_at < now() - interval '10 minutes' then
    raise exception 'Ce match a expiré';
  end if;

  update matches set player2_id = auth.uid(), status = 'drafting' where id = p_match_id;

  insert into match_teams (match_id, user_id, budget_remaining)
  values (p_match_id, auth.uid(), v_match.budget);
end;
$$ language plpgsql security definer;

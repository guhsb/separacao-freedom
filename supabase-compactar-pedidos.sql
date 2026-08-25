-- Compacta pedidos finalizados há mais de dois dias.
-- Preserva os dados principais e remove apenas a lista detalhada de produtos.
-- Execute uma vez no SQL Editor do Supabase.

create or replace function public.compactar_pedidos_finalizados(p_dias integer default 2)
returns integer
language plpgsql
security invoker
set search_path = public
as $$
declare
  quantidade integer;
begin
  with candidatos as (
    select id
    from public.pedidos
    where (dados->>'horaFim') is not null
      and coalesce((dados->>'compactado')::boolean, false) = false
      and ((dados->>'horaFim')::timestamptz) < now() - make_interval(days => greatest(p_dias, 0))
  ), atualizados as (
    update public.pedidos p
    set dados = jsonb_set(
      jsonb_set(
        p.dados - 'itens',
        '{compactado}', 'true'::jsonb, true
      ),
      '{compactadoEm}', to_jsonb(now()), true
    ),
    atualizado_em = now()
    from candidatos c
    where p.id = c.id
    returning p.id
  )
  select count(*) into quantidade from atualizados;

  return quantidade;
end;
$$;

grant execute on function public.compactar_pedidos_finalizados(integer) to anon, authenticated;

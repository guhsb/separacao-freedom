-- Reserva transacional de pedidos para impedir que dois separadores
-- iniciem o mesmo pedido simultaneamente.
-- Execute este arquivo uma vez no SQL Editor do Supabase.

create or replace function public.reservar_pedido(p_id text, p_dados jsonb)
returns jsonb
language plpgsql
security invoker
set search_path = public
as $$
begin
  insert into public.pedidos (id, dados, atualizado_em)
  values (p_id, p_dados, now())
  on conflict (id) do nothing;

  if not found then
    raise exception 'JA_INICIADO';
  end if;

  return p_dados;
end;
$$;

create or replace function public.reservar_pedidos_lote(p_pedidos jsonb)
returns jsonb
language plpgsql
security invoker
set search_path = public
as $$
declare
  pedido jsonb;
  pedido_id text;
begin
  for pedido in select value from jsonb_array_elements(p_pedidos)
  loop
    pedido_id := pedido->>'id';
    if pedido_id is null or pedido_id = '' then
      raise exception 'ID_PEDIDO_INVALIDO';
    end if;

    insert into public.pedidos (id, dados, atualizado_em)
    values (pedido_id, pedido, now())
    on conflict (id) do nothing;

    if not found then
      raise exception 'JA_INICIADO:%', pedido_id;
    end if;
  end loop;

  return p_pedidos;
end;
$$;

grant execute on function public.reservar_pedido(text, jsonb) to anon, authenticated;
grant execute on function public.reservar_pedidos_lote(jsonb) to anon, authenticated;

-- As policies atuais da tabela pedidos precisam permitir INSERT.
-- A função não importa nem copia nenhum dado antigo.

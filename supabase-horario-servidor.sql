-- Retorna o horário do banco para sincronizar o relógio do aplicativo.
-- Execute uma vez no SQL Editor do Supabase.

create or replace function public.horario_servidor()
returns timestamptz
language sql
stable
security invoker
set search_path = public
as $$
  select now();
$$;

grant execute on function public.horario_servidor() to anon, authenticated;

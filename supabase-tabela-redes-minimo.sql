-- Redes com regra própria de valor mínimo.
-- Uma rede cadastrada aqui não segue o mínimo da região: usa o valor informado,
-- ou fica isenta quando o valor é 0.
--
-- Rode uma vez no SQL Editor do Supabase.

create table if not exists public.redes_minimo (
  id text primary key,
  dados jsonb not null default '{}'::jsonb,
  atualizado_em timestamptz not null default now()
);

alter table public.redes_minimo enable row level security;

drop policy if exists "acesso_total_redes_minimo" on public.redes_minimo;
create policy "acesso_total_redes_minimo"
  on public.redes_minimo
  for all
  using (true)
  with check (true);

do $$
begin
  alter publication supabase_realtime add table public.redes_minimo;
exception
  when duplicate_object then
    raise notice 'Tabela redes_minimo ja estava no tempo real, seguindo.';
end $$;

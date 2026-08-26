-- Tabela de presença: registra quem está com o app aberto.
-- Serve para o Dashboard mostrar "ativo" mesmo quando a pessoa não tem pedido
-- em andamento no momento.
--
-- Rode uma vez no SQL Editor do Supabase.

create table if not exists public.presenca (
  id text primary key,
  dados jsonb not null default '{}'::jsonb,
  atualizado_em timestamptz not null default now()
);

alter table public.presenca enable row level security;

drop policy if exists "acesso_total_presenca" on public.presenca;
create policy "acesso_total_presenca"
  on public.presenca
  for all
  using (true)
  with check (true);

-- Habilita atualização em tempo real (não falha se já estiver publicada)
do $$
begin
  alter publication supabase_realtime add table public.presenca;
exception
  when duplicate_object then
    raise notice 'Tabela presenca ja estava no tempo real, seguindo.';
end $$;

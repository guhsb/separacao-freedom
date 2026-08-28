-- Tabela das regiões com valor mínimo de pedido.
-- Cada região junta cidades e/ou estados inteiros. Pedidos abaixo do mínimo da
-- sua região travam automaticamente — exceto pedidos de rede, que são isentos.
--
-- Rode uma vez no SQL Editor do Supabase.

create table if not exists public.regioes_minimo (
  id text primary key,
  dados jsonb not null default '{}'::jsonb,
  atualizado_em timestamptz not null default now()
);

alter table public.regioes_minimo enable row level security;

drop policy if exists "acesso_total_regioes_minimo" on public.regioes_minimo;
create policy "acesso_total_regioes_minimo"
  on public.regioes_minimo
  for all
  using (true)
  with check (true);

do $$
begin
  alter publication supabase_realtime add table public.regioes_minimo;
exception
  when duplicate_object then
    raise notice 'Tabela regioes_minimo ja estava no tempo real, seguindo.';
end $$;

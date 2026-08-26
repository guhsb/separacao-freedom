-- Tabela para os avisos de item faltando.
-- Criada quando um separador para um pedido por falta de item: quem cuida da
-- Área de Pedidos recebe o aviso e o item aparece como gargalo no Dashboard.
--
-- Rode uma vez no SQL Editor do Supabase.

create table if not exists public.avisos_itens (
  id text primary key,
  dados jsonb not null default '{}'::jsonb,
  atualizado_em timestamptz not null default now()
);

alter table public.avisos_itens enable row level security;

-- Mesma política das demais tabelas do app
drop policy if exists "acesso_total_avisos_itens" on public.avisos_itens;
create policy "acesso_total_avisos_itens"
  on public.avisos_itens
  for all
  using (true)
  with check (true);

-- Habilita atualização em tempo real.
-- O "do $$" evita erro caso a tabela já esteja publicada (ao rodar o script
-- mais de uma vez).
do $$
begin
  alter publication supabase_realtime add table public.avisos_itens;
exception
  when duplicate_object then
    raise notice 'Tabela avisos_itens ja estava no tempo real, seguindo.';
end $$;

-- Atualiza a versão e o link do APK sem apagar outras configurações de config/app.
-- Execute uma vez no SQL Editor do Supabase.

create or replace function public.publicar_versao_apk(p_versao integer, p_url text)
returns void
language plpgsql
security invoker
set search_path = public
as $$
begin
  insert into public.config (id, dados, atualizado_em)
  values (
    'app',
    jsonb_build_object('versaoApk', p_versao, 'apkUrl', p_url),
    now()
  )
  on conflict (id) do update
    set dados = coalesce(public.config.dados, '{}'::jsonb)
               || jsonb_build_object('versaoApk', p_versao, 'apkUrl', p_url),
        atualizado_em = now();
end;
$$;

grant execute on function public.publicar_versao_apk(integer, text) to service_role;

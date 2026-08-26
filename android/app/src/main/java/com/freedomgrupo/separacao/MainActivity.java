package com.freedomgrupo.separacao;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsIntent;

import com.getcapacitor.BridgeActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends BridgeActivity {

    /** Endereco do app publicado, usado quando o WebView do aparelho e antigo demais. */
    private static final String URL_APP = "https://guhsb.github.io/separacao-freedom/";

    /** Abaixo desta versao do motor Chrome, o WebView nao da conta do app. */
    private static final int VERSAO_MINIMA_WEBVIEW = 70;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = getBridge().getWebView();
        webView.addJavascriptInterface(new AndroidDownloader(this), "AndroidDownloader");
        webView.addJavascriptInterface(new AndroidPrinter(this, webView), "AndroidPrinter");

        // Coletores de dados com Android 7/8 vem com o WebView travado numa versao
        // antiga que o fabricante nao deixa atualizar, e nela o app nao consegue
        // se conectar ao servidor. Nesses aparelhos, abrimos o app pelo Chrome
        // instalado (que atualiza normalmente) em vez do WebView do sistema.
        if (webViewEhAntigo(webView)) {
            abrirNoChrome();
        }
    }

    /** Le a versao do motor do WebView a partir do user agent. */
    private boolean webViewEhAntigo(WebView webView) {
        try {
            String ua = WebSettings.getDefaultUserAgent(this);
            if (ua == null) return false;
            int i = ua.indexOf("Chrome/");
            if (i < 0) return true; // sem Chrome no user agent: WebView muito antigo
            String resto = ua.substring(i + 7);
            int ponto = resto.indexOf('.');
            if (ponto < 0) return false;
            int versao = Integer.parseInt(resto.substring(0, ponto));
            return versao < VERSAO_MINIMA_WEBVIEW;
        } catch (Exception e) {
            return false;
        }
    }

    /** Abre o app no Chrome do aparelho, mantendo a aparencia de aplicativo. */
    private void abrirNoChrome() {
        try {
            CustomTabsIntent intent = new CustomTabsIntent.Builder()
                    .setShowTitle(false)
                    .setUrlBarHidingEnabled(true)
                    .build();
            intent.intent.setPackage("com.android.chrome");
            intent.launchUrl(this, Uri.parse(URL_APP));
            finish();
        } catch (Exception e) {
            // Sem Chrome instalado: tenta qualquer navegador disponivel.
            try {
                CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
                intent.launchUrl(this, Uri.parse(URL_APP));
                finish();
            } catch (Exception e2) {
                Toast.makeText(this,
                        "Este aparelho precisa do Chrome instalado para rodar o app.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Ponte JS -> Android para salvar arquivos gerados no app (ex: relatorio em Excel)
     * diretamente na pasta Downloads do celular. Necessario porque o WebView do
     * Android nao processa downloads via Blob/anchor como um navegador comum faz.
     *
     * Uso no JavaScript da pagina:
     *   window.AndroidDownloader.saveBase64File(base64Data, "arquivo.xlsx", "application/vnd...")
     */
    public static class AndroidDownloader {
        private final Context context;

        AndroidDownloader(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void saveBase64File(String base64Data, String filename, String mimeType) {
            try {
                byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    if (uri != null) {
                        OutputStream out = context.getContentResolver().openOutputStream(uri);
                        if (out != null) {
                            out.write(bytes);
                            out.close();
                        }
                    }
                } else {
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs();
                    }
                    File file = new File(downloadsDir, filename);
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(bytes);
                    out.close();
                }

                showToast("Arquivo salvo em Downloads: " + filename);
            } catch (Exception e) {
                showToast("Erro ao salvar arquivo: " + e.getMessage());
            }
        }

        private void showToast(final String message) {
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    /**
     * Ponte JS -> Android para abrir a caixa de impressao nativa do sistema
     * (que inclui a opcao "Salvar como PDF"), a partir da pagina web do app.
     * Funciona 100% offline, sem depender de nenhuma biblioteca externa.
     *
     * Uso no JavaScript da pagina:
     *   window.AndroidPrinter.printPage()
     */
    public static class AndroidPrinter {
        private final Context context;
        private final WebView webView;

        AndroidPrinter(Context context, WebView webView) {
            this.context = context;
            this.webView = webView;
        }

        @JavascriptInterface
        public void printPage() {
            if (!(context instanceof Activity)) return;
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
                        String jobName = "Relatorio_Separacao_Freedom";
                        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
                        if (printManager != null) {
                            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, "Erro ao abrir impressão: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}

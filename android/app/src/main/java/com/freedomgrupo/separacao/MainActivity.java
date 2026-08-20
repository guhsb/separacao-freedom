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
import android.widget.Toast;

import com.getcapacitor.BridgeActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = getBridge().getWebView();
        webView.addJavascriptInterface(new AndroidDownloader(this), "AndroidDownloader");
        webView.addJavascriptInterface(new AndroidPrinter(this, webView), "AndroidPrinter");
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

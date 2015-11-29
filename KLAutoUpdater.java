package com.klifa.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Federico "Klifa" Sendra on 17/04/15.
 */

public class KLAutoUpdater
{
    private ProgressDialog progressDialog;
    private Activity activity;
    private String updateUrl;
    private PostUpdateCallback callback;
    private UpdateApp actualizaApp;

    //Textos
    private String titleProgressDownloading = "Descargando nueva actualizaci\00f3n...";
    private String titleUpdateAvailable = "Actualizaci\u00f3n Disponible";
    private String titleForceUpdateAvailable = "Debe actualizar para poder continuar";
    private String titleErrorDownloading = "Error";
    private String contentErrorDownloading = "Hubo un error al descargar la actualizaci\00f3n, intente nuevamente";
    private String neutralButtonError = "Aceptar";
    private String titleSearchingNewUpdate = "Por favor espere ...";
    private String contentSearchingNewUpdate = "Buscando actualizaci\00f3n";
    private String downloadButton = "Descargar";
    private String ignoreButton = "Ignorar";
    private String exitButton = "Salir";
	
	//Tags
	private String VERSION_CODE = "versionCode";
	private String APK_URL = "apkUrl";
	private String TEXT_CONTENT = "default";
	private String FORCE_UPDATE = "forceUpdate";

    public KLAutoUpdater(Activity activity, String url, PostUpdateCallback callback)
    {
        this.updateUrl = url;
        this.activity = activity;
        this.callback=callback;
    }

    public void checkearUpdate()
    {
        new CheckUpdateTask().execute();
    }

    public class UpdateApp extends AsyncTask<Void,Integer,Boolean> {

        private String path;
        String urlApk;

        public UpdateApp(String url2)
        {
            urlApk = url2;
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                URL url = new URL(this.urlApk);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.connect();

                long fileLength = c.getContentLength();

                String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                File file = new File(PATH);
                file.mkdirs();
                File outputFile = new File(file, "update.apk");
                if(outputFile.exists()){
                    outputFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                long total = 0;
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    total+=len1;
                    publishProgress(Math.round(total * 100 / fileLength));
                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();

                path= outputFile.getAbsolutePath();

            } catch (Exception e) {
                Log.e("UpdateAPP", "Update error! " + e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage(titleProgressDownloading);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(true);
            progressDialog.setProgress(0);
            progressDialog.setMax(100);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();
            if (success) {
                Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.fromFile(new File(path)),
                                "application/vnd.android.package-archive");
                promptInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(promptInstall);
                activity.finish();
            }else
            {
                AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setTitle(titleErrorDownloading);
                alertDialog.setMessage(contentErrorDownloading);
                alertDialog.setCancelable(false);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, neutralButtonError,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                activity.finish();
                            }
                        });
                alertDialog.show();
            }
        }
    }

    public class CheckUpdateTask extends AsyncTask<Void,Void,Boolean> {

        private String apkUrl;
        private String descUpdate;
        private boolean forceUpdate;

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                URL url = new URL(updateUrl);
                URLConnection conn = url.openConnection();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(conn.getInputStream());

                int versionCode = Integer.parseInt(doc.getElementsByTagName(VERSION_CODE).item(0).getTextContent());
                apkUrl = doc.getElementsByTagName(APK_URL).item(0).getTextContent();
                descUpdate = doc.getElementsByTagName(TEXT_CONTENT).item(0).getTextContent();
                forceUpdate = Boolean.parseBoolean(doc.getElementsByTagName(FORCE_UPDATE).item(0).getTextContent());

                PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
                int installedVersionCode = pInfo.versionCode;

                if (installedVersionCode<versionCode)
                {
                    return true;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(activity, titleSearchingNewUpdate, contentSearchingNewUpdate, true);
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();
            if (success) {
                if (forceUpdate) {
                    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog.setTitle(titleForceUpdateAvailable);
                    alertDialog.setMessage(descUpdate);
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, downloadButton,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    actualizaApp = new UpdateApp(apkUrl);
                                    actualizaApp.execute();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, exitButton,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    activity.finish();
                                }
                            });
                    alertDialog.show();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog.setTitle(titleUpdateAvailable);
                    alertDialog.setMessage(descUpdate);
                    alertDialog.setCancelable(false);
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, downloadButton,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    actualizaApp = new UpdateApp(apkUrl);
                                    actualizaApp.execute();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, ignoreButton,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    callback.postUpdate();
                                }
                            });
                    alertDialog.show();
                }
            } else {
                callback.postUpdate();
            }
        }
    }
	
    /*
	*Se llama a este metodo cuando no hay actualizacion o ignora la actualizacion
    *y continua la ejecucion de la app
	*/
    public interface PostUpdateCallback
    {
        void postUpdate();
    }

    public void setTextStrings(String titleProgressDownloading,
                               String titleUpdateAvailable,
                               String titleForceUpdateAvailable,
                               String titleErrorDownloading,
                               String contentErrorDownloading,
                               String neutralButtonError,
                               String titleSearchingNewUpdate,
                               String contentSearchingNewUpdate,
                               String downloadButton,
                               String ignoreButton,
                               String exitButton)
    {
        this.titleProgressDownloading = titleProgressDownloading;
        this.titleUpdateAvailable = titleUpdateAvailable;
        this.titleForceUpdateAvailable = titleForceUpdateAvailable;
        this.titleErrorDownloading = titleErrorDownloading;
        this.contentErrorDownloading = contentErrorDownloading;
        this.neutralButtonError = neutralButtonError;
        this.titleSearchingNewUpdate = titleSearchingNewUpdate;
        this.contentSearchingNewUpdate = contentSearchingNewUpdate;
        this.downloadButton = downloadButton;
        this.ignoreButton = ignoreButton;
        this.exitButton = exitButton;
    }

    public void setTitleProgressDownloading(String titleProgressDownloading) {
        this.titleProgressDownloading = titleProgressDownloading;
    }

    public void setTitleUpdateAvailable(String titleUpdateAvailable) {
        this.titleUpdateAvailable = titleUpdateAvailable;
    }

    public void setTitleForceUpdateAvailable(String titleForceUpdateAvailable) {
        this.titleForceUpdateAvailable = titleForceUpdateAvailable;
    }

    public void setTitleErrorDownloading(String titleErrorDownloading) {
        this.titleErrorDownloading = titleErrorDownloading;
    }

    public void setContentErrorDownloading(String contentErrorDownloading) {
        this.contentErrorDownloading = contentErrorDownloading;
    }

    public void setNeutralButtonError(String neutralButtonError) {
        this.neutralButtonError = neutralButtonError;
    }

    public void setTitleSearchingNewUpdate(String titleSearchingNewUpdate) {
        this.titleSearchingNewUpdate = titleSearchingNewUpdate;
    }

    public void setContentSearchingNewUpdate(String contentSearchingNewUpdate) {
        this.contentSearchingNewUpdate = contentSearchingNewUpdate;
    }

    public void setDownloadButton(String downloadButton) {
        this.downloadButton = downloadButton;
    }

    public void setIgnoreButton(String ignoreButton) {
        this.ignoreButton = ignoreButton;
    }

    public void setExitButton(String exitButton) {
        this.exitButton = exitButton;
    }
}

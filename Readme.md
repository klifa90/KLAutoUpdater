KLAutoUpdater
===================
by Klifa

KLAutoUpdater is a library that will automatically update the application using the latest version available.

## How it works
1) It checks against a hosted .xml file which is the latest available version and compares it with the one installed.
2) If there is a new version, obtains a download URL of the latest APK and starts downloading it.
3) When it finish downloading, a prompt message for installing appears.

## How to add it in my project?
1) Download KLAutoUpdater.java and add it to your project
2) Simply use it like this:

KLAutoUpdater autoUpdate = new KLAutoUpdater(MyActivity.this, StringWithURLOfHostedXMLFile, new KLAutoUpdater.PostUpdateCallback() {
            @Override
            public void postUpdate() {
                realizarLogin();
            }
        });
autoUpdate.checkearUpdate();

postUpdate() handles the logic invoked when there is no update available or update was ignored (because it blocks the UI).

## XML Formatting and customization options

versionCode (int) = versionCode to compare installation version
apkUrl (string) = URL of latest APK
default (string) = Informational patch notes to show to the user
forceUpdate (boolean) = If the update should block the use of the application or not (if yes, the user will not be able to use the application before updating)

NOTE:
If forceUpdate == true ,then 2 options will be available: download new version or quit application.
If forceUpdate == false, then the 2 options will be: download new version or ignore.

## Strings

Customizables strings are

titleProgressDownloading = "Descargando nueva actualización...";
titleUpdateAvailable = "Actualización Disponible";
titleForceUpdateAvailable = "Debe actualizar para poder continuar";
titleErrorDownloading = "Error";
contentErrorDownloading = "Hubo un error al descargar la actualización, intente nuevamente";
neutralButtonError = "Aceptar";
titleSearchingNewUpdate = "Por favor espere ...";
contentSearchingNewUpdate = "Buscando actualización";
downloadButton = "Descargar";
ignoreButton = "Ignorar";
exitButton = "Salir";

They can be changed one by one, or all together by calling autoUpdate.setTextStrings(...) 


## License

    The MIT License (MIT)

    Copyright (c) 2015 Klifa(http://www.spacedevuy.com)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
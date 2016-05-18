package fr.utarwyn.endercontainers.utils;

import fr.utarwyn.endercontainers.EnderContainers;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Updater {
    private static EnderContainers plugin;

    public String address = Config.updateBase + EnderContainers.getInstance().getDescription().getName() + "-lastest.jar";
    public String updatepath = "plugins" + File.separator + EnderContainers.getInstance().getDescription().getName() + ".jar";


    public Updater(EnderContainers instance) {
        plugin = instance;
    }

    public String getNewVersion(){
        String newVersion = null;
        String version    = EnderContainers.getInstance().getDescription().getVersion();

        try {
            URL url = new URL(Config.updateBase + "version");
            InputStream is = url.openStream();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) >= 0)
                os.write(buf, 0, n);
            os.close();
            is.close();
            byte[] data = os.toByteArray();

            newVersion = new String(data);

            if (newVersion.equalsIgnoreCase(version)) newVersion = null;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return newVersion;
    }

    public void update() {
        plugin.getLogger().info("Updating...");
        download();
    }

    public void update(String adress, String file) {
        plugin.getLogger().info("Updating...");
        download(adress, file);
    }

    @SuppressWarnings("unused")
    public void download() {
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        try {
            URL url = new URL(this.address);
            out = new BufferedOutputStream(new FileOutputStream(this.updatepath));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
                numWritten += numRead;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused")
    public void download(String adress, String file) {
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        try {
            URL url = new URL(adress);
            out = new BufferedOutputStream(new FileOutputStream(file));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
                numWritten += numRead;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
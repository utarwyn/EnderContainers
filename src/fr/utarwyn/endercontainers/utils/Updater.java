package fr.utarwyn.endercontainers.utils;

import fr.utarwyn.endercontainers.EnderContainers;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Updater {
    private static EnderContainers plugin;

    public String address = "http://185.13.38.245/plugins/EnderContainers/" + EnderContainers.getInstance().getDescription().getName() + "-lastest.jar";
    public String updatepath = "plugins" + File.separator + EnderContainers.getInstance().getDescription().getName() + ".jar";

    public Updater(EnderContainers instance) {
        plugin = instance;
    }

    public Boolean updateCheck() {
        URLConnection conn = null;
        try {
            URL url = new URL(this.address);
            conn = url.openConnection();
            File localfile = new File(this.updatepath);
            long lastmodifiedurl = conn.getLastModified();
            long lastmodifiedfile = localfile.lastModified();
            if (lastmodifiedurl > lastmodifiedfile) {
                plugin.getLogger().info("Mise a jour en cours...");
                download();
                return true;
            } else {
                plugin.getLogger().info("Aucun mise a jour trouvee !");
                return false;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.getServer().notify();
        return false;
    }

    public void update() {
        plugin.getLogger().info("Mise a jour en cours...");
        download();
    }

    public void update(String adress, String file) {
        plugin.getLogger().info("Mise a jour en cours...");
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
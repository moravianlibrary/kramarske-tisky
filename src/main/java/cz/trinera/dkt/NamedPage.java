package cz.trinera.dkt;

import java.io.File;
import java.util.UUID;

public class NamedPage {

    private int position;
    private final String name;
    private final File pngImageFile;
    private final File tifImageFile;
    private final UUID uuid = UUID.randomUUID();

    public NamedPage(int position, String name, File pngImageFile, File tifImageFile) {
        this.position = position;
        this.name = name;
        this.pngImageFile = pngImageFile;
        this.tifImageFile = tifImageFile;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public File getPngImageFile() {
        return pngImageFile;
    }

    public File getTifImageFile() {
        return tifImageFile;
    }

    public UUID getUuid() {
        return uuid;
    }

    public NamedPage withDifferentPngImageFile(File destPngImageFile) {
        return new NamedPage(position, name, destPngImageFile, tifImageFile);
    }

    @Override
    public String toString() {
        return "NamedPage{" +
                "position=" + position +
                ", name='" + name + '\'' +
                ", pngImageFile=" + pngImageFile.getAbsolutePath() +
                ", tifImageFile=" + tifImageFile.getAbsolutePath() +
                ", uuid=" + uuid +
                '}';
    }
}

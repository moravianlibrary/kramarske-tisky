package cz.trinera.dkt;

import java.io.File;

public class NamedPage {

    private final int position;
    private final String name;
    private final File pngImageFile;
    private final File tifImageFile;

    public NamedPage(int position, String name, File pngImageFile, File tifImageFile) {
        this.position = position;
        this.name = name;
        this.pngImageFile = pngImageFile;
        this.tifImageFile = tifImageFile;
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
                '}';
    }
}

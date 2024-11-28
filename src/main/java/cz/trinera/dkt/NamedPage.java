package cz.trinera.dkt;

import java.io.File;

public class NamedPage {

    private final int position;
    private final String name;
    private final File imageFile;

    public NamedPage(int position, String name, File imageFile) {
        this.position = position;
        this.name = name;
        this.imageFile = imageFile;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public File getImageFile() {
        return imageFile;
    }

    public NamedPage withDifferentFile(File destFile) {
        return new NamedPage(position, name, destFile);
    }

    @Override
    public String toString() {
        return "NamedPage{" +
                "position=" + position +
                ", name='" + name + '\'' +
                ", imageFile=" + imageFile.getAbsolutePath() +
                '}';
    }
}

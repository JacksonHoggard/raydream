package me.jacksonhoggard.raydream.gui.editor.material;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class Texture {
    private final String path;
    private int id;

    public Texture(String path) throws IOException {
        this.path = path;
        loadTexture(path);
    }

    private void loadTexture(String path) throws IOException {
        int width;
        int height;
        ByteBuffer buffer;
        try (MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            File file = new File(path);
            String filePath = file.getAbsolutePath();
            buffer = STBImage.stbi_load(filePath, w, h, channels, 4);
            if(buffer ==null) {
                throw new Exception("Can't load file "+ path +" "+ STBImage.stbi_failure_reason());
            }
            width = w.get();
            height = h.get();

            glEnable(GL_TEXTURE_2D);
            this.id = glGenTextures();
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, id);
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
            glGenerateMipmap(GL_TEXTURE_2D);
            STBImage.stbi_image_free(buffer);
        } catch(Exception e) {
            throw new IOException("Failed to load texture.", e);
        }

    }

    public int getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void remove() {
        glDeleteTextures(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Texture texture)) return false;
        return id == texture.id && Objects.equals(path, texture.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, id);
    }
}

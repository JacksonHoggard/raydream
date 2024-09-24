package me.jacksonhoggard.raydream.render;

public interface RenderCancelListener {
    void cancel();
    boolean isCanceled();
    void setCanceled(boolean canceled);
}

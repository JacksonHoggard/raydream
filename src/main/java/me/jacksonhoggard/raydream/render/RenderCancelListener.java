package me.jacksonhoggard.raydream.render;

public interface RenderCancelListener {
    void cancel();
    boolean isCancelled();
    void setCancelled(boolean cancelled);
}

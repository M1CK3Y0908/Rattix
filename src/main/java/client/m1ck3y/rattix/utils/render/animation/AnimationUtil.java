package client.m1ck3y.rattix.utils.render.animation;

/**
 * UI rendering animation helper.
 * Aligned with LiquidBounce utils/render/animation/AnimationUtil.
 */
public class AnimationUtil {

    public static float animate(float target, float current, float speed) {
        boolean larger = target > current;
        if (speed < 0.0F) {
            speed = 0.0F;
        } else if (speed > 1.0F) {
            speed = 1.0F;
        }
        float dif = Math.abs(target - current);
        float factor = dif * speed;
        if (factor < 0.1F) {
            factor = 0.1F;
        }
        if (larger) {
            if (current + factor > target) {
                current = target;
            } else {
                current += factor;
            }
        } else {
            if (current - factor < target) {
                current = target;
            } else {
                current -= factor;
            }
        }
        return current;
    }
}

package client.m1ck3y.rattix.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
    private static final EventManager INSTANCE = new EventManager();

    private final Map<Class<? extends Event>, List<ListenerMethod>> registry = new HashMap<>();

    public static EventManager getInstance() {
        return INSTANCE;
    }

    private static class ListenerMethod {
        final Object target;
        final Method method;
        final byte priority;

        ListenerMethod(Object target, Method method, byte priority) {
            this.target = target;
            this.method = method;
            this.priority = priority;
        }
    }

    @SuppressWarnings("unchecked")
    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventTarget.class) && method.getParameterTypes().length == 1) {
                Class<?> paramType = method.getParameterTypes()[0];
                if (Event.class.isAssignableFrom(paramType)) {
                    EventTarget annotation = method.getAnnotation(EventTarget.class);
                    method.setAccessible(true);
                    Class<? extends Event> eventClass = (Class<? extends Event>) paramType;
                    registry.computeIfAbsent(eventClass, k -> new ArrayList<>())
                            .add(new ListenerMethod(listener, method, annotation.priority()));
                }
            }
        }
    }

    public void unregister(Object listener) {
        for (List<ListenerMethod> list : registry.values()) {
            list.removeIf(lm -> lm.target == listener);
        }
    }

    public void call(Event event) {
        List<ListenerMethod> list = registry.get(event.getClass());
        if (list != null) {
            for (ListenerMethod lm : list) {
                try {
                    lm.method.invoke(lm.target, event);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}

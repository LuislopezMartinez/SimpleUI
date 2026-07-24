import simpleui.android.UIElement;
import simpleui.android.UIView;
import simpleui.android.UIViewManager;

/**
 * Compile-time guard for Android. @Override fails compilation if either hook
 * stops being public to Processing sketches.
 */
public final class AndroidUIViewHooksCompileSmoke {
    private static final class ProbeElement extends UIElement {
        ProbeElement(String id) {
            super(id, 10, 10, 120, 44);
        }

        @Override
        public void draw() {
        }
    }

    private static final class ProbeView extends UIView {
        ProbeView(UIViewManager manager) {
            super("android-probe", manager);
        }

        @Override
        public void initialize() {
            addControl(new ProbeElement("button"));
        }

        @Override
        public void onUIEvent(UIElement element, String action, Object data) {
        }

        @Override
        public void update() {
        }
    }
}

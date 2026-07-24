import simpleui.desktop.UIElement;
import simpleui.desktop.SimpleUI;
import simpleui.desktop.UIView;
import simpleui.desktop.UIViewManager;

/**
 * Verifies UIView hooks from the default package, matching how Processing
 * sketches subclass library classes.
 */
public final class DesktopUIViewHooksSmoke {
    private static final class ProbeElement extends UIElement {
        ProbeElement(String id) {
            super(id, 10, 10, 120, 40);
        }

        @Override
        public void draw() {
        }
    }

    private static final class ProbeView extends UIView {
        ProbeElement button;
        int initializeCalls;
        int eventCalls;
        int updateCalls;
        int showCalls;
        int hideCalls;
        int destroyCalls;

        ProbeView(UIViewManager manager) {
            super("probe", manager);
        }

        @Override
        public void initialize() {
            initializeCalls++;
            button = new ProbeElement("probeButton");
            addControl(button);
        }

        @Override
        public void onUIEvent(UIElement element, String action, Object data) {
            if (element != button) {
                throw new AssertionError("UIView received an event from an unknown control");
            }
            if (!"clicked".equals(action)) {
                throw new AssertionError("Unexpected UIView action: " + action);
            }
            eventCalls++;
        }

        @Override
        public void update() {
            updateCalls++;
        }

        @Override
        public void onShow() {
            showCalls++;
        }

        @Override
        public void onHide() {
            hideCalls++;
        }

        @Override
        public void onDestroy() {
            destroyCalls++;
        }
    }

    public static void main(String[] args) {
        SimpleUI.clearUIElements();
        SimpleUI.uiViewManagers.clear();

        UIViewManager manager = new UIViewManager("view-hooks");
        ProbeView view = new ProbeView(manager);

        manager.show(view);
        require(view.initializeCalls == 1, "initialize() was not called exactly once");
        require(view.showCalls == 1, "onShow() was not called");
        require(view.isVisible(), "View was not made visible");
        require(view.button != null, "View did not create its control");

        boolean routed = manager.routeEvent(view.button, "clicked", null);
        require(routed, "UIViewManager did not route the owned control event");
        require(view.eventCalls == 1, "onUIEvent() override was not invoked");

        manager.update();
        require(view.updateCalls == 1, "update() override was not invoked");

        manager.hide(view);
        require(view.hideCalls == 1, "onHide() was not called");
        require(!view.isVisible(), "View remained visible after hide()");

        manager.destroy(view);
        require(view.destroyCalls == 1, "onDestroy() was not called");
        require(!view.isInitialized(), "View remained initialized after destroy()");

        SimpleUI.clearUIElements();
        SimpleUI.uiViewManagers.clear();
        System.out.println("Desktop UIView external hooks passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

import processing.core.PApplet;
import processing.event.MouseEvent;
import simpleui.desktop.*;

public class DesktopEventBridgeSmoke {
    private static final class TestApplet extends PApplet {
        void dispatch(MouseEvent event) { handleMouseEvent(event); }
    }

    public static void main(String[] args) {
        TestApplet host = new TestApplet();
        host.width = 320;
        host.height = 240;
        SimpleUI.attach(host);
        if (!SimpleUI.isEventBridgeInstalled()) {
            throw new AssertionError("Event bridge was not registered");
        }

        final int[] clicks = {0};
        final int[] dropdownChanges = {0};
        final int[] sliderChanges = {0};
        final int[] sliderReleases = {0};
        final int[] switchChanges = {0};
        final int[] legacyEvents = {0};
        UIButton button = new UIButton("test", 10, 10, 100, 40, "Test", 14);
        UIDropdown dropdown = new UIDropdown("mode", 10, 60, 100, 30,
            new String[]{"A", "B"}, 14);
        UISlider slider = new UISlider("level", 10, 120, 100, 24, 0, 100, 20, 14);
        UITable table = new UITable("devices", 150, 10, 150, 100,
            new String[]{"MAC"}, new float[]{1.0f}, 14);
        UISwitch modeSwitch = new UISwitch(
            "modeSwitch", 10, 160, 130, 32, "Intensidad", false, 14
        );
        for (int i = 0; i < 10; i++) {
            table.addRow(new String[]{"device-" + i});
        }
        SimpleUI.addUIElement(button);
        SimpleUI.addUIElement(dropdown);
        SimpleUI.addUIElement(slider);
        SimpleUI.addUIElement(table);
        SimpleUI.addUIElement(modeSwitch);
        SimpleUI.setUIEventHandler(new UIEventHandler() {
            public void onUIEvent(UIElement element, String action, Object data) {
                if (element == button && "clicked".equals(action)) clicks[0]++;
                if (element == dropdown && "changed".equals(action)) dropdownChanges[0]++;
                if (element == slider && "changed".equals(action)) sliderChanges[0]++;
                if (element == slider && "released".equals(action)) sliderReleases[0]++;
                if (element == modeSwitch && "changed".equals(action)) switchChanges[0]++;
                if ("selected".equals(action) || "valueChanged".equals(action)) legacyEvents[0]++;
            }
        });
        host.dispatch(new MouseEvent(host, 1, MouseEvent.PRESS, 0, 20, 20, 1, 1));
        host.dispatch(new MouseEvent(host, 2, MouseEvent.RELEASE, 0, 20, 20, 1, 1));
        if (clicks[0] != 1) {
            throw new AssertionError("Expected one automatic click, got " + clicks[0]);
        }

        dropdown.handleClickAt(20, 70);
        dropdown.handleClickAt(20, 125);
        if (dropdownChanges[0] != 1 || dropdown.getSelectedIndex() != 1) {
            throw new AssertionError("Dropdown must emit one changed event");
        }

        slider.updateFromMouse(60);
        slider.beginGestureDrag();
        slider.forceRelease();
        if (sliderChanges[0] < 1 || sliderReleases[0] != 1 || legacyEvents[0] != 0) {
            throw new AssertionError("Slider event contract mismatch");
        }

        host.dispatch(new MouseEvent(host, 3, MouseEvent.WHEEL, 0, 200, 50, 0, 1));
        if (table.internalScrollY >= 0) {
            throw new AssertionError("Mouse wheel must scroll the table under the cursor");
        }
        host.dispatch(new MouseEvent(host, 4, MouseEvent.WHEEL, 0, 200, 50, 0, -100));
        if (table.internalScrollY != 0) {
            throw new AssertionError("Mouse wheel scrolling must stop at the top boundary");
        }
        if (table.scrollbarThumbHeight() >= table.scrollbarTrackHeight()) {
            throw new AssertionError("Overflowing table must use a proportional scrollbar thumb");
        }
        if (Math.abs(table.scrollbarThumbY() - table.scrollbarTrackTop()) > 0.01f) {
            throw new AssertionError("Scrollbar thumb must start at the top");
        }
        table.internalScrollY = table.minimumScroll();
        float expectedBottom = table.scrollbarTrackTop() +
            table.scrollbarTrackHeight() - table.scrollbarThumbHeight();
        if (Math.abs(table.scrollbarThumbY() - expectedBottom) > 0.01f) {
            throw new AssertionError("Scrollbar thumb must reach the bottom");
        }
        table.internalScrollY = 0;
        table.performTapAction(
            table.scrollbarTrackX() + table.scrollbarWidth() * 0.5f,
            table.scrollbarTrackTop() + table.scrollbarTrackHeight() - 1
        );
        if (table.internalScrollY >= 0) {
            throw new AssertionError("Clicking the scrollbar rail must page down");
        }
        modeSwitch.performTapAction(20, 170);
        if (!modeSwitch.isChecked() || switchChanges[0] != 1) {
            throw new AssertionError("Switch must toggle and emit one changed event");
        }
        modeSwitch.setEnabled(false);
        modeSwitch.performTapAction(20, 170);
        if (!modeSwitch.isChecked() || switchChanges[0] != 1) {
            throw new AssertionError("Disabled switch must ignore taps");
        }

        host.width = 1600;
        host.height = 1000;
        SimpleUI.syncHostState();
        SimpleUI.setMode(1280, 720);
        if (Math.abs(SimpleUI.uiScale - 1.25f) > 0.001f ||
            Math.abs(SimpleUI.viewportOffsetX) > 0.001f ||
            Math.abs(SimpleUI.viewportOffsetY - 50) > 0.001f) {
            throw new AssertionError("FIT mode viewport calculation mismatch");
        }
        float screenX = SimpleUI.designToScreenX(320);
        float screenY = SimpleUI.designToScreenY(180);
        if (Math.abs(SimpleUI.screenToDesignX(screenX) - 320) > 0.001f ||
            Math.abs(SimpleUI.screenToDesignY(screenY) - 180) > 0.001f) {
            throw new AssertionError("Viewport coordinate conversion must round-trip");
        }
        SimpleUI.setMode(1280, 720, UIScaleMode.RESPONSIVE);
        if (Math.abs(SimpleUI.getLogicalWidth() - 1280) > 0.001f ||
            Math.abs(SimpleUI.getLogicalHeight() - 800) > 0.001f ||
            Math.abs(SimpleUI.viewportOffsetY) > 0.001f) {
            throw new AssertionError("RESPONSIVE mode logical viewport mismatch");
        }

        SimpleUI.detach();
        if (SimpleUI.isEventBridgeInstalled()) {
            throw new AssertionError("Event bridge was not removed");
        }
        System.out.println("Desktop event bridge lifecycle passed.");
    }
}

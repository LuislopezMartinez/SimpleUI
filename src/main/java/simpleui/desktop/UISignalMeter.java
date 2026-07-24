package simpleui.desktop;

import java.util.*;
import processing.core.*;
import processing.event.*;
import static processing.core.PApplet.*;
import static processing.core.PConstants.*;
import static simpleui.desktop.SimpleUI.*;

public class UISignalMeter extends UIElement {
  public String label;
  public int rssiDbm = 0;
  public boolean pending = false;

  public UISignalMeter(String id, int x, int y, int w, int h, String label) {
    super(id, x, y, w, h);
    this.label = label == null ? "" : label;
  }

  public void setLabel(String nextLabel) {
    label = nextLabel == null ? "" : nextLabel;
  }

  public void setRssi(int nextRssiDbm) {
    rssiDbm = nextRssiDbm;
  }

  public void setPending(boolean nextPending) {
    pending = nextPending;
  }

  public String smeterLabelFromRssi() {
    if (rssiDbm < -121) {
      return "S0";
    }
    if (rssiDbm <= -73) {
      int sLevel = ((rssiDbm + 121) / 6) + 1;
      sLevel = constrain(sLevel, 1, 9);
      return "S" + sLevel;
    }
    return "S9+" + (rssiDbm + 73);
  }

  public int smeterSegmentsFromRssi() {
    if (rssiDbm < -121) {
      return 0;
    }
    if (rssiDbm <= -73) {
      int sLevel = ((rssiDbm + 121) / 6) + 1;
      return constrain(sLevel, 1, 9);
    }
    int plusDb = rssiDbm + 73;
    if (plusDb >= 30) return 12;
    if (plusDb >= 20) return 11;
    if (plusDb >= 10) return 10;
    return 9;
  }

  public void draw() {
    if (!isVisible) return;
    pushStyle();
    stroke(72, 66, 44);
    strokeWeight(1.5f);
    fill(247, 238, 196);
    rect(x, y, width, height, 8);

    fill(44, 42, 36);
    textAlign(LEFT, TOP);
    textSize(10);
    text(label, x + 8, y + 6);
    textAlign(RIGHT, TOP);
    String headerText;
    if (pending) {
      headerText = "waiting";
    } else {
      String rssiText = rssiDbm == 0 ? "--" : (rssiDbm + " dBm");
      headerText = smeterLabelFromRssi() + "  " + rssiText;
    }
    text(headerText, x + width - 8, y + 6);

    float meterTop = y + 22;
    float meterBottom = y + height - 14;
    float barAreaHeight = meterBottom - meterTop;
    float padding = 10;
    int segmentCount = 12;
    float stepW = (width - padding * 2) / segmentCount;
    int litSegments = rssiDbm == 0 ? 0 : smeterSegmentsFromRssi();

    for (int i = 0; i < segmentCount; i++) {
      float sx = x + padding + i * stepW + 1;
      float sw = max(6, stepW - 4);
      float normalized = (i + 1) / (float)segmentCount;
      float sh = 8 + normalized * (barAreaHeight - 8);
      float sy = meterBottom - sh;

      if (pending) {
        int phase = (millis() / 140) % segmentCount;
        int distance = abs(i - phase);
        int wrappedDistance = min(distance, segmentCount - distance);
        if (wrappedDistance == 0) {
          fill(217, 122, 33);
        } else if (wrappedDistance == 1) {
          fill(217, 122, 33, 150);
        } else {
          fill(110, 104, 92, 45);
        }
      } else if (i < litSegments) {
        if (i <= 3) {
          fill(38, 154, 83);
        } else if (i <= 6) {
          fill(212, 182, 33);
        } else if (i <= 8) {
          fill(217, 122, 33);
        } else {
          fill(184, 48, 48);
        }
      } else {
        fill(110, 104, 92, 45);
      }
      noStroke();
      rect(sx, sy, sw, sh, 1.5f);
    }

    stroke(44, 42, 36);
    strokeWeight(1);
    line(x + padding, y + height - 8, x + width - padding, y + height - 8);
    popStyle();
  }
}

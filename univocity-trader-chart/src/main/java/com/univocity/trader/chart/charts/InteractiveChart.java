package com.univocity.trader.chart.charts;


import com.univocity.trader.candles.*;
import com.univocity.trader.chart.*;
import com.univocity.trader.chart.charts.controls.*;

import java.awt.*;
import java.awt.event.*;

public abstract class InteractiveChart<C extends InteractiveChartController> extends BasicChart<C> {

	private Point mousePosition = null;
	private boolean mouseDragging = false;

	public InteractiveChart(CandleHistoryView candleHistory) {
		super(candleHistory);
		this.setFocusable(true);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mouseDragging = false;
				Candle current = getCurrentCandle();
				Candle selected = getSelectedCandle();

				if (current != selected) {
					setSelectedCandle(current);
				} else {
					setSelectedCandle(null);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mouseDragging = false;
				invokeRepaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				mouseDragging = true;
				invokeRepaint();
			}
		});

		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				mouseDragging = true;
				processMouseEvent(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				processMouseEvent(e);
			}

			private void processMouseEvent(final MouseEvent e) {
				mousePosition = e.getPoint();
				if (inDisabledSection(mousePosition)) {
					return;
				}
				mousePosition.x = translateX(mousePosition.x);
				Candle candle = getCandleUnderCursor();
				if (candle != getCurrentCandle()) {
					setCurrentCandle(candle);
				}
				invokeRepaint();
			}
		});

		addMouseListener(new MouseAdapter() {
			public void mouseExited(MouseEvent e) {
				mousePosition = null;
			}
		});
	}

	public boolean isMouseDragging(){
		return mouseDragging && mousePosition != null && mousePosition.getY() < getHeight() - getScrollHeight();
	}

	public Candle getCandleUnderCursor() {
		if (mousePosition != null) {
			return getCandleAtCoordinate(mousePosition.x);
		}
		return null;
	}

	private boolean isVerticalSelectionLineEnabled() {
		return getController().isVerticalSelectionLineEnabled();
	}

	private boolean isHorizontalSelectionLineEnabled() {
		return getController().isHorizontalSelectionLineEnabled();
	}

	private Color getSelectionLineColor() {
		return getController().getSelectionLineColor();
	}

	public final Point getCurrentMousePosition() {
		return mousePosition;
	}

	@Override
	protected void draw(Graphics2D g, int width) {
		Point hoveredPosition = getCurrentCandleLocation();

		if (isVerticalSelectionLineEnabled() || isHorizontalSelectionLineEnabled()) {
			g.setStroke(new BasicStroke(1));
			if (hoveredPosition != null) {
				g.setColor(getSelectionLineColor());
				if (isVerticalSelectionLineEnabled()) {
					g.drawLine(hoveredPosition.x, 0, hoveredPosition.x, height);
				}
				if (isHorizontalSelectionLineEnabled()) {
					g.drawLine(0, hoveredPosition.y, width, hoveredPosition.y);
				}
			}
		}

		Point selectionPoint = getSelectedCandleLocation();
		if (selectionPoint != null) {
			drawSelected(getSelectedCandle(), selectionPoint, g);
		}
		if (hoveredPosition != null) {
			drawHovered(getCurrentCandle(), hoveredPosition, g);
		}
	}

	protected final Stroke getLineStroke() {
		return getController().getNormalStroke();
	}

	protected abstract void drawSelected(Candle selected, Point location, Graphics2D g);

	protected abstract void drawHovered(Candle hovered, Point location, Graphics2D g);
}

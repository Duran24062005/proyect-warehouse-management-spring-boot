function getLibrary() {
  const library = window.LightweightCharts;
  if (!library) {
    throw new Error("Lightweight Charts no esta disponible en la pagina.");
  }
  return library;
}

function formatLegendDate(value) {
  if (!value) return "Sin fecha";
  return new Intl.DateTimeFormat("es-CO", {
    dateStyle: "medium"
  }).format(new Date(`${value}T00:00:00`));
}

function createEmptyState(host, message) {
  host.innerHTML = `<div class="chart-empty-state">${message}</div>`;
  return {
    destroy() {
      host.innerHTML = "";
    }
  };
}

export function createMovementAnalyticsChart(container, payload, options = {}) {
  const { legendContainer, emptyMessage = "No hay datos suficientes para graficar este periodo." } = options;

  if (!container) {
    return null;
  }

  const hasData = Array.isArray(payload?.series) && payload.series.some((series) => series.data?.some((point) => point.value > 0));
  if (!hasData) {
    return createEmptyState(container, emptyMessage);
  }

  const { createChart, LineSeries } = getLibrary();
  container.innerHTML = "";
  const chartHost = document.createElement("div");
  chartHost.className = "analytics-chart-canvas";
  container.append(chartHost);

  const chart = createChart(chartHost, {
    width: Math.max(320, container.clientWidth || 320),
    height: 320,
    layout: {
      textColor: "#dbe7f5",
      background: { type: "solid", color: "transparent" }
    },
    grid: {
      vertLines: { color: "rgba(148, 163, 184, 0.1)" },
      horzLines: { color: "rgba(148, 163, 184, 0.1)" }
    },
    rightPriceScale: {
      borderColor: "rgba(148, 163, 184, 0.18)"
    },
    timeScale: {
      borderColor: "rgba(148, 163, 184, 0.18)"
    },
    crosshair: {
      vertLine: {
        color: "rgba(56, 189, 248, 0.35)",
        labelBackgroundColor: "#0f172a"
      },
      horzLine: {
        color: "rgba(250, 204, 21, 0.25)",
        labelBackgroundColor: "#0f172a"
      }
    }
  });

  const seriesRefs = payload.series.map((series) => {
    const lineSeries = chart.addSeries(LineSeries, {
      color: series.color,
      lineWidth: series.id === "total" ? 3 : 2,
      crosshairMarkerVisible: true,
      priceLineVisible: false,
      lastValueVisible: false
    });
    lineSeries.setData(series.data);
    return { config: series, lineSeries };
  });

  chart.timeScale().fitContent();

  const renderLegend = (time, valuesMap = new Map()) => {
    if (!legendContainer) return;

    const selectedTime = time || payload.points?.[payload.points.length - 1]?.time;
    const entries = payload.series.map((series) => {
      const currentValue = valuesMap.get(series.id) ?? series.data?.[series.data.length - 1]?.value ?? 0;
      return `
        <div class="chart-legend-item">
          <span class="chart-legend-dot" style="background:${series.color}"></span>
          <span class="chart-legend-label">${series.label}</span>
          <strong class="chart-legend-value">${currentValue}</strong>
        </div>
      `;
    });

    legendContainer.innerHTML = `
      <div class="chart-legend-date">${formatLegendDate(selectedTime)}</div>
      <div class="chart-legend-grid">
        ${entries.join("")}
      </div>
    `;
  };

  renderLegend();

  const crosshairHandler = (param) => {
    if (!param?.time) {
      renderLegend();
      return;
    }

    const valueMap = new Map();
    seriesRefs.forEach(({ config, lineSeries }) => {
      const data = param.seriesData.get(lineSeries);
      valueMap.set(config.id, data?.value ?? 0);
    });
    renderLegend(param.time, valueMap);
  };

  chart.subscribeCrosshairMove(crosshairHandler);

  const observer = new ResizeObserver((entries) => {
    const entry = entries[0];
    if (!entry) return;
    chart.resize(Math.max(320, Math.floor(entry.contentRect.width)), 320);
  });
  observer.observe(container);

  return {
    destroy() {
      observer.disconnect();
      chart.unsubscribeCrosshairMove(crosshairHandler);
      chart.remove();
      container.innerHTML = "";
      if (legendContainer) {
        legendContainer.innerHTML = "";
      }
    }
  };
}

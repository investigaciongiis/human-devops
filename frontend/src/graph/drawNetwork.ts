import {
  D3DragEvent,
  drag,
  forceCenter,
  forceCollide,
  forceLink,
  forceManyBody,
  forceSimulation,
  interpolateRdYlGn,
  select,
} from "d3";

import { Data, Node } from "./GraphData";

export function drawNetwork({
  data,
  width,
  height,
  svgRef,
}: {
  data: Data;
  width: number;
  height: number;
  svgRef: any;
}) {
  const colors = interpolateRdYlGn;

  const nodes = data.nodes.map((d) => Object.create(d));
  const links = data.links.map((d) => Object.create(d));

  console.log(nodes);

  const simulation = forceSimulation(nodes)
    .force(
      "link",
      forceLink(links).id((d: any) => d.id)
    )
    .force(
      "charge",
      forceManyBody()
        .strength(-100)
        .distanceMax(Math.min(width, height) * 0.18)
    )
    .force("collide", forceCollide(10))
    .force("center", forceCenter(width / 2, height / 2));

  const linksG = select(svgRef.current)
    .select("g.links")
    .attr("stroke", "gray")
    .attr("stroke-width", 1)
    .attr("stroke-opacity", 0.5)
    .selectAll("line")
    .data(links)
    .join("line");

  const nodesG = select(svgRef.current)
    .select("g.nodes")
    .selectAll("circle")
    .data(nodes)
    .join<any>("circle")
    .attr("fill", (d: any) => (d.score !== undefined && d.score !== "" && d.score !== null ? colors(d.score) : "gray"))
    .attr("fill-opacity", 0.7)
    .attr("r", 8);

const centerDots = select(svgRef.current)
  .select("g.nodes")
  .selectAll("circle.center-dot")
  .data(
    nodes.filter((d: any) => !d.affectedBy || d.affectedBy.length === 0),
    (d: any) => d.id
  )
  .join("circle")
  .attr("class", "center-dot")
  .attr("r", 1.5)
  .attr("fill", "#065f46")
  .attr("pointer-events", "none");

  nodesG.call(
    drag<any, any>()
      .on("start", dragstarted)
      .on("drag", dragged)
      .on("end", dragended)
  );

  nodesG
    .on("pointerenter pointermove", mouseover)
    .on("pointerleave", mouseleft);

  const tooltip = select(svgRef.current).select("g.tooltip");

  function dragstarted(
    this: SVGCircleElement,
    event: D3DragEvent<SVGCircleElement, Node, any>
  ) {
    simulation.alphaTarget(0.3).restart();
    event.subject.fx = event.subject.x;
    event.subject.fy = event.subject.y;
    select(this).attr("stroke", "black");
  }

  function dragged(event: D3DragEvent<SVGCircleElement, Node, any>) {
    event.subject.fx = event.x;
    event.subject.fy = event.y;
  }

  function dragended(
    this: SVGCircleElement,
    event: D3DragEvent<SVGCircleElement, Node, any>
  ) {
    simulation.alphaTarget(0);
    event.subject.fx = null;
    event.subject.fy = null;
    select(this).attr("stroke", null);
  }

  function mouseover(this: SVGCircleElement, event: MouseEvent, d: Node) {
    select(this).attr("r", 10);
    tooltip
      .attr("transform", `translate(${event.offsetX}, ${event.offsetY})`)
      .style("display", null);

    const rect = tooltip
      .selectAll("rect")
      .data([,])
      .join("rect")
      .attr("x", 0)
      .attr("y", 0)
      .attr("rx", 5)
      .attr("ry", 5);

    const text = tooltip
      .selectAll("text")
      .data([,])
      .join("text")
      .attr("fill", "var(--text-color)")
      .call((text) =>
        text
          .selectAll("tspan")
          .data(() => [d.title, 
            (d.score !== undefined && d.score !== null ? (d.score*100).toFixed(0)+'%' : "Unmeasured")+
            (!d.affectedBy || d.affectedBy.length === 0 ? " - Measured factor" : " - Inferred factor")])
          .join("tspan")
          .attr("x", 0)
          .attr("y", (_: any, i: any) => `${i * 1.1}em`)
          .attr("font-weight", (_: any, i: any) => (i ? null : "bold"))
          .text((d: any) => d)
      );

    size(text, rect);
  }

  function mouseleft(this: SVGCircleElement, event: MouseEvent) {
    select(this).attr("r", 8);
    tooltip.style("display", "none");
  }

  function size(text: any, rect: any) {
    const { x, y, width: w, height: h } = text.node().getBBox();
    text.attr("transform", `translate(${-w / 2},${y - 25})`);
    rect
      .attr("width", w + 20)
      .attr("height", h + 10)
      .attr("transform", `translate(${-w / 2 - 10},${y - 45})`);
  }

  simulation.on("tick", () => {
    linksG
      .attr("x1", (d: any) => d.source.x)
      .attr("y1", (d: any) => d.source.y)
      .attr("x2", (d: any) => d.target.x)
      .attr("y2", (d: any) => d.target.y);

    nodesG.attr("cx", (d: any) => d.x).attr("cy", (d: any) => d.y);

    centerDots.attr("cx", (d: any) => d.x).attr("cy", (d: any) => d.y);
  });

const svg = select(svgRef.current);
svg.selectAll(".color-legend").remove();

const legend = svg.append("g")
  .attr("class", "color-legend")
  .attr("transform", `translate(${width - 160}, 20)`);

legend.append("rect")
  .attr("width", 150)
  .attr("height", 145)
  .attr("rx", 6)
  .attr("ry", 6)
  .attr("fill", "rgba(255,255,255,0.9)")
  .attr("stroke", "none")
  .attr("style", 'stroke: none !important');

legend.append("text")
  .attr("x", 10)
  .attr("y", 18)
  .text("Human Factor Score")
  .attr("font-size", "12px")
  .attr("font-weight", "bold")
  .attr("fill", "#222");

const legendValues = ["gray", 0.0, 0.25, 0.5, 0.75, 1.0];
const legendLabels = ["Unmeasured", "0%", "25%", "50%", "75%", "100%"];

legend.selectAll("rect.color-box")
  .data(legendValues)
  .enter()
  .append("rect")
  .attr("class", "color-box")
  .attr("x", 10)
  .attr("y", (d, i) => 28 + i * 20)
  .attr("width", 20)
  .attr("height", 15)
  .attr("rx", 2)
  .attr("ry", 2)
  .attr("fill", d => d === "gray" ? "gray" : colors(Number(d)))
  .attr("style", d => `fill: ${d === "gray" ? "gray" : colors(Number(d))} !important; fill-opacity: 1 !important;`)
  .attr("stroke", "#333")
  .attr("stroke-width", 0.5);

legend.selectAll("text.color-label")
  .data(legendLabels)
  .enter()
  .append("text")
  .attr("class", "color-label")
  .attr("x", 40)
  .attr("y", (d, i) => 40 + i * 20)
  .attr("font-size", "11px")
  .attr("fill", "#222")
  .text(d => d);

const measuredY = 40 + legendValues.length * 20;

legend.append("circle")
  .attr("cx", 20)
  .attr("cy", measuredY - 4)
  .attr("r", 2)
  .attr("fill", "#065f46")
  .attr("stroke", "#333")
  .attr("stroke-width", 0.5);

legend.append("text")
  .attr("x", 40)
  .attr("y", measuredY)
  .attr("font-size", "11px")
  .attr("fill", "#222")
  .text("Measured factor");
}

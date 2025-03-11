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
          .data(() => [d.title, d.score !== undefined && d.score !== null ? d.score : "Unmeasured"])
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
  });
}

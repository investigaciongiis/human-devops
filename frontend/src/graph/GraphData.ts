export interface Node extends d3.SimulationNodeDatum {
  id: number;
  title: string;
  cluster: string;
  score: number;
}

export interface Link extends d3.SimulationLinkDatum<Node> {
  source: number;
  target: number;
}

export type Data = {
  nodes: Node[];
  links: Link[];
};
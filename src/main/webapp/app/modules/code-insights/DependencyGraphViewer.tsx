import React, { useEffect, useState } from 'react';
import ReactFlow, {
  ReactFlowProvider,
  Background,
  Controls,
  Node,
  Edge,
  Position,
  MarkerType,
} from 'reactflow';
import 'reactflow/dist/style.css';
import dagre from 'dagre';

const nodeWidth = 180;
const nodeHeight = 60;

type GraphData = {
  nodes: { id: string; label: string }[];
  edges: { source: string; target: string }[];
};

const createLayoutedElements = (graphData: GraphData, direction: 'TB' | 'LR' = 'TB') => {
  const dagreGraph = new dagre.graphlib.Graph();
  dagreGraph.setDefaultEdgeLabel(() => ({}));
  dagreGraph.setGraph({
    rankdir: direction,
    ranksep: 150,
    nodesep: 100,
    marginx: 50,
    marginy: 50,
  });

  graphData.nodes.forEach((node) => {
    dagreGraph.setNode(node.id, { width: nodeWidth, height: nodeHeight });
  });

  graphData.edges.forEach((edge) => {
    // 👇 Reverse the direction here!
    dagreGraph.setEdge(edge.target, edge.source);
  });

  dagre.layout(dagreGraph);

  const nodes: Node[] = graphData.nodes.map((node) => {
    const dagreNode = dagreGraph.node(node.id);
    return {
      id: node.id,
      data: { label: node.label.split('/').pop() },
      position: { x: dagreNode.x - nodeWidth / 2, y: dagreNode.y - nodeHeight / 2 },
      style: {
        width: nodeWidth,
        height: nodeHeight,
        border: '1px solid #888',
        padding: '10px',
        borderRadius: 8,
        background: '#f8f9fa',
        fontSize: 14,
        textAlign: 'center',
        boxShadow: '0 2px 6px rgba(0,0,0,0.1)',
      },
      draggable: true,
      sourcePosition: direction === 'LR' ? Position.Right : Position.Bottom,
      targetPosition: direction === 'LR' ? Position.Left : Position.Top,
    };
  });

  const edges: Edge[] = graphData.edges.map((edge) => ({
    id: `e-${edge.source}-${edge.target}`,
    source: edge.target,   // reversed!
    target: edge.source,   // reversed!
    markerEnd: {
      type: MarkerType.ArrowClosed,
      width: 20,
      height: 20,
      color: '#555',
    },
    style: { strokeWidth: 2 },
  }));

  return { nodes, edges };
};

const DependencyGraphViewer = ({ graphData }: { graphData: GraphData }) => {
  const [nodes, setNodes] = useState<Node[]>([]);
  const [edges, setEdges] = useState<Edge[]>([]);

  useEffect(() => {
    const layouted = createLayoutedElements(graphData, 'TB');
    setNodes(layouted.nodes);
    setEdges(layouted.edges);
  }, [graphData]);

  return (
    <div style={{ height: 600, background: '#fff', borderRadius: 8, overflow: 'hidden' }}>
      <ReactFlowProvider>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          fitView
          fitViewOptions={{ padding: 0.3 }}
          nodesDraggable={true}
          panOnDrag
          nodeDragThreshold={0} // respond instantly to drags
        >
          <Background color="#f0f0f0" gap={16} />
          <Controls />
        </ReactFlow>
      </ReactFlowProvider>
    </div>
  );
};

export default DependencyGraphViewer;

import React, { useMemo } from 'react';
import ReactFlow, {
  Background,
  Controls,
  Edge,
  Node,
  MiniMap
} from 'reactflow';
import 'reactflow/dist/style.css';

const DependencyGraphViewer = ({ graphData }: { graphData: any }) => {
  const { nodes, edges } = useMemo(() => {
    const flowNodes: Node[] = (graphData?.nodes || []).map((node: any) => ({
      id: node.id.toString(),
      data: { label: node.label },
      position: { x: Math.random() * 400, y: Math.random() * 400 }, // or use a layout lib
      type: 'default',
    }));

    const flowEdges: Edge[] = (graphData?.edges || []).map((edge: any) => ({
      id: `${edge.source}-${edge.target}`,
      source: edge.source.toString(),
      target: edge.target.toString(),
      animated: true,
      type: 'default',
    }));

    return { nodes: flowNodes, edges: flowEdges };
  }, [graphData]);

  return (
    <div className="mt-5">
      <h4 className="text-info">📈 Dependency Graph</h4>
      <div
        style={{ height: '600px', background: '#fff', borderRadius: '8px' }}
        className="shadow-sm"
      >
        <ReactFlow nodes={nodes} edges={edges} fitView>
          <MiniMap />
          <Controls />
          <Background />
        </ReactFlow>
      </div>
    </div>
  );
};

export default DependencyGraphViewer;

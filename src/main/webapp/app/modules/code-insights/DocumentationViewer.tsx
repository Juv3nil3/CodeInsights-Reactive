import React from 'react';

const DocumentationViewer = ({ data }: { data: any }) => {
  return (
    <div className="mt-4">
      <h5>Generated Documentation</h5>
      <pre style={{ background: '#f4f4f4', padding: '1rem' }}>
        {JSON.stringify(data, null, 2)}
      </pre>
    </div>
  );
};

export default DocumentationViewer;

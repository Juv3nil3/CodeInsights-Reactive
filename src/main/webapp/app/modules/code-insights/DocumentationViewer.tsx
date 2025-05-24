import React from 'react';

const DocumentationViewer = ({ data }: { data: any }) => {
  return (
    <div className="mt-4">
      <h5>Generated Documentation</h5>
      <pre
        style={{
          backgroundColor: '#1e1e1e',
          color: '#dcdcdc',
          padding: '1rem',
          borderRadius: '8px',
          overflowX: 'auto',
        }}
      >
        {JSON.stringify(data, null, 2)}
      </pre>

    </div>
  );
};

export default DocumentationViewer;

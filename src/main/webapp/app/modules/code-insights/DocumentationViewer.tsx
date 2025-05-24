import React, { useState } from 'react';

const ToggleSection = ({ title, children, defaultExpanded = true }: any) => {
  const [expanded, setExpanded] = useState(defaultExpanded);

  return (
    <div className="ms-2">
      <div
        className="cursor-pointer fw-bold mb-1"
        onClick={() => setExpanded(!expanded)}
        style={{ userSelect: 'none' }}
      >
        {expanded ? '🔽' : '▶️'} {title}
      </div>
      {expanded && <div className="ms-3">{children}</div>}
    </div>
  );
};

const DocumentationViewer = ({ data }: { data: any }) => {
  if (!data) return null;

  const renderComment = (comment: string | null) => {
    if (!comment) return null;
    return (
      <div className="text-light small mt-1" style={{ whiteSpace: 'pre-wrap' }}>
        {comment.replace(/\r\n/g, '\n').trim()}
      </div>
    );
  };

  const renderFields = (fields: any[]) => (
    <ul>
      {fields.map((field, i) => (
        <li key={i} className="text-light">
          🧩 <strong>{field.name}</strong>
          {renderComment(field.comment)}
        </li>
      ))}
    </ul>
  );

  const renderMethods = (methods: any[]) => (
    <ul>
      {methods.map((method, i) => (
        <li key={i} className="text-light">
          🔧 <strong>{method.name}</strong>
          {renderComment(method.comment)}
        </li>
      ))}
    </ul>
  );

  const renderClasses = (classes: any[]) => {
    return classes.map((cls, i) => (
      <ToggleSection key={i} title={`📘 Class: ${cls.name}`}>
        {renderComment(cls.comment)}
        {cls.fields?.length > 0 && (
          <>
            <div className="fw-bold mt-2">Fields:</div>
            {renderFields(cls.fields)}
          </>
        )}
        {cls.methods?.length > 0 && (
          <>
            <div className="fw-bold mt-2">Methods:</div>
            {renderMethods(cls.methods)}
          </>
        )}
      </ToggleSection>
    ));
  };

  const renderFiles = (files: any[]) => {
    return files.map((file, i) => (
      <ToggleSection key={i} title={`📄 ${file.fileName || 'Unnamed File'}`} defaultExpanded={false}>
        {renderClasses(file.classes)}
      </ToggleSection>
    ));
  };

  const renderPackages = (packages: any[], level = 0) => {
    return packages.map((pkg, i) => (
      <ToggleSection key={i} title={`📦 ${pkg.packageName}`} defaultExpanded={false}>
        {pkg.files && renderFiles(pkg.files)}
        {pkg.subPackages && renderPackages(pkg.subPackages, level + 1)}
      </ToggleSection>
    ));
  };

  return (
    <div className="p-4 rounded shadow-sm" style={{ backgroundColor: '#1e1e1e', color: '#ffffff' }}>
      <h2 className="mb-3 text-success">📙 {data.repoName}</h2>
      <p>
        <strong>Owner:</strong> {data.owner} | <strong>Branch:</strong> {data.branchName}
      </p>
      <p className="text-muted">
        Created: {new Date(data.createdAt).toLocaleString()} | Updated: {new Date(data.updatedAt).toLocaleString()}
      </p>

      <div className="mt-4">
        <h4 className="text-primary">📂 Packages</h4>
        {renderPackages(data.packages)}
      </div>
    </div>
  );
};

export default DocumentationViewer;

import React from 'react';

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

  const renderFields = (fields: any[]) => {
    return fields.map((field, i) => (
      <li key={i} className="ms-3 text-light">
        🧩 <strong>{field.name}</strong>
        {renderComment(field.comment)}
      </li>
    ));
  };

  const renderMethods = (methods: any[]) => {
    return methods.map((method, i) => (
      <li key={i} className="ms-3 text-light">
        🔧 <strong>{method.name}</strong>
        {renderComment(method.comment)}
      </li>
    ));
  };

  const renderClasses = (classes: any[]) => {
    return classes.map((cls, i) => (
      <div key={i} className="ms-3 mt-3 text-light">
        <h6 className="text-info">📘 Class: <strong>{cls.name}</strong></h6>
        {renderComment(cls.comment)}
        {cls.fields?.length > 0 && (
          <>
            <div className="fw-bold mt-2">Fields:</div>
            <ul>{renderFields(cls.fields)}</ul>
          </>
        )}
        {cls.methods?.length > 0 && (
          <>
            <div className="fw-bold mt-2">Methods:</div>
            <ul>{renderMethods(cls.methods)}</ul>
          </>
        )}
      </div>
    ));
  };

  const renderFiles = (files: any[]) => {
    return files.map((file, i) => (
      <div key={i} className="ms-3 text-light">
        <div className="fw-bold">📄 {file.fileName || 'Unnamed File'}</div>
        {renderClasses(file.classes)}
      </div>
    ));
  };

  const renderPackages = (packages: any[], level = 0) => {
    return packages.map((pkg, i) => (
      <div key={i} className={`ms-${level * 3} mt-3`}>
        <h5 className="text-warning">📦 {pkg.packageName}</h5>
        {pkg.files && renderFiles(pkg.files)}
        {pkg.subPackages && renderPackages(pkg.subPackages, level + 1)}
      </div>
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

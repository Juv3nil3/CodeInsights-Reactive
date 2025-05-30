import React, { useState } from 'react';
import DependencyGraphViewer from './DependencyGraphViewer';

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

const filterClassesInDocument = (document, searchClassName) => {
  const filteredPackages = document.packages
    ?.map(pkg => {
      const filteredFiles = pkg.files
        ?.map(file => {
          const filteredClasses = file.classes
            ?.filter(cls => cls.className === searchClassName);
          return (filteredClasses && filteredClasses.length > 0)
            ? { ...file, classes: filteredClasses }
            : null;
        })
        ?.filter(file => file !== null);

      return (filteredFiles && filteredFiles.length > 0)
        ? { ...pkg, files: filteredFiles }
        : null;
    })
    ?.filter(pkg => pkg !== null);

  return { ...document, packages: filteredPackages };
};


const DocumentationStats = ({ stats }: { stats: any }) => {
  if (!stats) return null;
  return (
    <div className="p-3 text-light bg-dark rounded shadow-sm" style={{ minWidth: '220px' }}>
      <h5 className="text-success mb-3">📊 Stats</h5>
      <ul className="list-unstyled small">
        <li>📦 Packages: <strong>{stats.totalPackages}</strong></li>
        <li>📄 Files: <strong>{stats.totalFiles}</strong></li>
        <li>📘 Classes: <strong>{stats.totalClasses}</strong></li>
        <li>🔧 Methods: <strong>{stats.totalMethods}</strong></li>
        <li>🧩 Fields: <strong>{stats.totalFields}</strong></li>
      </ul>
    </div>
  );
};

const DocumentationViewer = ({ data }: { data: any }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [isSearching, setIsSearching] = useState(false);

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      return;
    }

    setIsSearching(true);
    try {
      const response = await fetch(`/api/documentation/search/class?className=${encodeURIComponent(searchQuery)}`);
      const results = await response.json();

      // Filter the classes on the frontend
      const filteredResults = results.map(doc => filterClassesInDocument(doc, searchQuery));

      setSearchResults(filteredResults);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setIsSearching(false);
    }
  };

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

  const renderSearchResults = () => {
    if (!Array.isArray(searchResults) || searchResults.length === 0) return null;
    return (
      <div className="mt-4">
        <h5 className="text-primary">🔎 Search Results:</h5>
        {searchResults.map((doc, index) => (
          <div key={index} className="mb-3 p-2 bg-dark text-light rounded">
            <h6>📙 {doc.documentationName || 'Unnamed Documentation'}</h6>
            {doc.packages?.map((pkg: any, pkgIndex: number) => (
              <div key={pkgIndex} className="ms-3">
                📦 <strong>{pkg.packageName}</strong>
                {pkg.files?.map((file: any, fileIndex: number) => (
                  <div key={fileIndex} className="ms-3">
                    📄 {file.filePath}
                    {file.classes?.map((cls: any, clsIndex: number) => (
                      <div key={clsIndex} className="ms-3">
                        📘 <strong>{cls.className}</strong>
                        <div className="small">{cls.comment}</div>
                      </div>
                    ))}
                  </div>
                ))}
              </div>
            ))}
          </div>
        ))}
      </div>
    );
  };


  if (!data) return null;

  return (
    <div className="d-flex gap-4 p-4" style={{ backgroundColor: '#1e1e1e', color: '#ffffff' }}>
      {/* Sidebar: Statistics */}
      <DocumentationStats stats={data.statistics} />

      {/* Main Content: Documentation Tree */}
      <div className="flex-grow-1">
        <h2 className="mb-3 text-success">📙 {data.repoName}</h2>
        <p>
          <strong>Owner:</strong> {data.owner} | <strong>Branch:</strong> {data.branchName}
        </p>
        <p className="text-muted">
          Created: {new Date(data.createdAt).toLocaleString()} | Updated: {new Date(data.updatedAt).toLocaleString()}
        </p>

        {/* Search Bar */}
        <div className="mb-3">
          <input
            type="text"
            placeholder="🔍 Search by class name..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyPress={(e) => { if (e.key === 'Enter') handleSearch(); }}
            className="form-control"
            style={{ maxWidth: '400px', display: 'inline-block', marginRight: '10px' }}
          />
          <button className="btn btn-primary" onClick={handleSearch} disabled={isSearching}>
            {isSearching ? 'Searching...' : 'Search'}
          </button>
        </div>

        {renderSearchResults()}

        {/* Documentation Tree */}
        <div className="mt-4">
          <h4 className="text-primary">📂 Packages</h4>
          {renderPackages(data.packages)}
        </div>

        {data.dependencyGraph?.nodes?.length > 0 && (
          <DependencyGraphViewer graphData={data.dependencyGraph} />
        )}
      </div>
    </div>
  );
};

export default DocumentationViewer;

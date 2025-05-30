import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Spinner } from 'reactstrap';
import BranchSelection from './BranchSelection';

const RepoSelection = () => {
  const [repos, setRepos] = useState<string[]>([]);
  const [selectedRepo, setSelectedRepo] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    axios.get('/api/github/repos').then(res => {
      setRepos(res.data);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  return (
    <div className="mt-4">
      <h4>Select Your Java Repository</h4>
      {loading && <Spinner />}
      {!loading && repos.length === 0 && <p>No repositories found</p>}

      {!loading && repos.length > 0 && (
        <div>
          {repos.map(repo => (
            <button
              key={repo}
              onClick={() => setSelectedRepo(repo)}
              style={{
                margin: '5px',
                padding: '8px 12px',
                cursor: 'pointer',
                backgroundColor: selectedRepo === repo ? '#007bff' : '#f0f0f0',
                color: selectedRepo === repo ? 'white' : 'black',
                border: '1px solid #ccc',
                borderRadius: '4px',
              }}
            >
              {repo}
            </button>
          ))}
        </div>
      )}

      {selectedRepo && (
        <div className="mt-4">
          <h5>Branches for: {selectedRepo}</h5>
          <BranchSelection repo={selectedRepo} />
        </div>
      )}
    </div>
  );
};

export default RepoSelection;

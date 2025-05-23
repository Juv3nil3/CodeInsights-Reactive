import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { FormGroup, Label, Input, Button, Spinner } from 'reactstrap';
import BranchSelection from './BranchSelection';

const RepoSelection = () => {
  const [repos, setRepos] = useState<string[]>([]);
  const [selectedRepo, setSelectedRepo] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    axios.get('/api/github/repos?language=Java').then(res => {
      setRepos(res.data);
      setLoading(false);
    });
  }, []);

  return (
    <div className="mt-4">
      <h4>Select a Java Repository</h4>
      {loading ? (
        <Spinner />
      ) : (
        <FormGroup>
          <Label for="repoSelect">Repository</Label>
          <Input
            type="select"
            id="repoSelect"
            onChange={e => setSelectedRepo(e.target.value)}
            value={selectedRepo || ''}
          >
            <option value="">-- Select --</option>
            {repos.map(repo => (
              <option key={repo} value={repo}>
                {repo}
              </option>
            ))}
          </Input>
        </FormGroup>
      )}

      {selectedRepo && <BranchSelection repo={selectedRepo} />}
    </div>
  );
};

export default RepoSelection;

import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Button, FormGroup, Label, Input, Spinner } from 'reactstrap';
import DocumentationViewer from './DocumentationViewer';

const BranchSelection = ({ repo }: { repo: string }) => {
  const [branches, setBranches] = useState<string[]>([]);
  const [selectedBranch, setSelectedBranch] = useState<string | null>(null);
  const [docData, setDocData] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    axios.get(`/api/github/repos/${repo}/branches`).then(res => {
      setBranches(res.data);
    });
  }, [repo]);

  const handleGenerate = () => {
    setLoading(true);
    axios
      .post('/api/docs/generate', { repo, branch: selectedBranch })
      .then(res => {
        setDocData(res.data);
        setLoading(false);
      });
  };

  return (
    <div className="mt-4">
      <h5>Branches of {repo}</h5>
      <FormGroup>
        <Label for="branchSelect">Branch</Label>
        <Input
          type="select"
          id="branchSelect"
          onChange={e => setSelectedBranch(e.target.value)}
          value={selectedBranch || ''}
        >
          <option value="">-- Select --</option>
          {branches.map(branch => (
            <option key={branch} value={branch}>
              {branch}
            </option>
          ))}
        </Input>
      </FormGroup>
      <Button
        disabled={!selectedBranch || loading}
        color="primary"
        onClick={handleGenerate}
      >
        {loading ? 'Generating...' : 'Generate Documentation'}
      </Button>

      {docData && <DocumentationViewer data={docData} />}
    </div>
  );
};

export default BranchSelection;

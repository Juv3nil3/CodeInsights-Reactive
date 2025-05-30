/* eslint-disable */

import './home.scss';
import RepoSelection from 'app/modules/code-insights/RepoSelection';


import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

import { Alert, Col, Row } from 'reactstrap';

import { REDIRECT_URL, getLoginUrl } from 'app/shared/util/url-utils';
import { useAppSelector } from 'app/config/store';

export const Home = () => {
  const account = useAppSelector(state => state.authentication.account);
  const pageLocation = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const redirectURL = localStorage.getItem(REDIRECT_URL);
    if (redirectURL) {
      localStorage.removeItem(REDIRECT_URL);
      location.href = `${location.origin}${redirectURL}`;
    }
  });

  return (
    <Row>
      <Col md="3" className="pad">
        <span className="hipster rounded" />
      </Col>
      <Col md="9">
        <h1 className="display-4">Welcome to Code Insights!</h1>
        <p className="lead">Generate Documentation for Your Java Repositories in few clicks</p>

        {account?.login ? (
          <>
            <Alert color="success">You are logged in as user &quot;{account.login}&quot;.</Alert>
            <RepoSelection />
          </>
        ) : (
          <Alert color="warning">
            <a
              className="alert-link"
              onClick={() =>
                navigate(getLoginUrl(), {
                  state: { from: pageLocation },
                })
              }
            >
              Login with your GitHub account
            </a>{' '}
            to get started.
          </Alert>
        )}
      </Col>
    </Row>
  );
};

export default Home;
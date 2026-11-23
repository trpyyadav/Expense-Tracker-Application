import { useCallback, useEffect, useRef, useState } from 'react';

// Runs an async loader and tracks loading / error / data. Re-runs when deps change.
export default function useAsync(loader, deps = []) {
  const [state, setState] = useState({ data: null, loading: true, error: null });
  const reqId = useRef(0);

  const run = useCallback(() => {
    const id = ++reqId.current;
    setState((s) => ({ ...s, loading: true, error: null }));
    loader()
      .then((data) => id === reqId.current && setState({ data, loading: false, error: null }))
      .catch((err) => id === reqId.current && setState({ data: null, loading: false, error: err.message }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => {
    run();
  }, [run]);

  return { ...state, reload: run };
}

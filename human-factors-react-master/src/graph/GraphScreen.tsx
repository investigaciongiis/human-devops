import { AxiosHeaders } from "axios";
import { useEffect, useState } from "react";
import AuthService from "../authentication/AuthService";
import { Data } from "./GraphData";
import "./GraphScreen.css";
import { Network } from "./Network";

const GraphScreen = () => {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<Data>();

  useEffect(() => {
    let mounted = true;
    AuthService
      .get("humanfactor", new AxiosHeaders())
      .then((data) => {
        if (mounted) {
          // Build the link array using the affectsTo field
          const links = data.data.flatMap((node: any) =>
            node.affectsTo.map((affectedId: any) => ({
              source: node.id,
              target: affectedId,
            }))
          );
          // Build the data array that Network expects
          const array: Data = {
            nodes: data.data,
            links: links
          };
          setData(array);
          setLoading(false);
          console.debug(data);
        }
      })
      .catch((error) => {
        // TODO: Handle error
      });
    return () => {
      mounted = false;
    };
  }, []);

  return (
    <div className="container-full">
      {loading && <div className="loading">Loading...</div>}
      {!loading && data !== undefined && <Network data={data} />}
    </div>
  );
};

export default GraphScreen;

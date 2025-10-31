import { AxiosHeaders } from "axios";
import { useEffect, useState } from "react";
import AuthService from "../authentication/AuthService";
import { Data } from "./GraphData";
import "./GraphScreen.css";
import { Network } from "./Network";

const GraphScreen = () => {
  const [loading, setLoading] = useState(true);
  const [emptyData, setEmpty] = useState(false);
  const [data, setData] = useState<Data>();

  async function downloadHumanFactors(format: "csv" | "json" | "xlsx") {
    try {
      const headers = new AxiosHeaders();

      if (format === "xlsx") {
        const url = `${process.env.REACT_APP_API_URL}/humanfactor/export?format=${format}`;
        const token = AuthService.getToken();
        headers["Accept"] = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        const res = await fetch(url, {
          headers: {
            Accept: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
        });

        if (!res.ok) {
          throw new Error(`Error downloading XLSX: ${res.status} ${res.statusText}`);
        }

        const blob = await res.blob();

        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        const ts = new Date();
        const pad = (n:number)=> String(n).padStart(2,"0");
        const filename = `human-factors_${ts.getFullYear()}-${pad(ts.getMonth()+1)}-${pad(ts.getDate())}T${pad(ts.getHours())}-${pad(ts.getMinutes())}-${pad(ts.getSeconds())}.xlsx`;
        link.download = filename;
        link.click();
        URL.revokeObjectURL(link.href);
        return;
      }

      const url = `humanfactor/export?format=${format}`;

      headers["Accept"] = format === "csv" ? "text/csv" : "application/json";

      const response = await AuthService.get(url, headers);

      const jsonString =
        typeof response.data === "string" ? response.data : JSON.stringify(response.data, null, 2);

      const blob = new Blob(
        [jsonString],
        { type: format === "csv" ? "text/csv;charset=utf-8" : "application/json;charset=utf-8" }
      );

      const date = new Date();
      const pad = (n: number) => n.toString().padStart(2, "0");

      const year = date.getFullYear();
      const month = pad(date.getMonth() + 1);
      const day = pad(date.getDate());
      const hours = pad(date.getHours());
      const minutes = pad(date.getMinutes());
      const seconds = pad(date.getSeconds());

      const formattedDate = `${year}-${month}-${day}_${hours}-${minutes}-${seconds}`;
    
      const fileName = `human-factors_${formattedDate}.${format}`;

      const link = document.createElement("a");
      link.href = URL.createObjectURL(blob);
      link.download = fileName;
      link.click();
      URL.revokeObjectURL(link.href);
    } catch (e) {
      console.error("Error downloading file:", e);
      alert("Error downloading file");
    }
  }

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
          let searched = false;
          for(var i=0; i<data.data.length; i++){
            if(data.data[i].score !== null){
              searched = true;
            }
          }
          if(!searched){
            setEmpty(true);
          }
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
    <div className="container-full" style={{ position: "relative" }}>
      {loading && <div className="loading">Loading...</div>}
      {emptyData && <h3>Insufficient data</h3>}
      {!loading && data !== undefined && <Network data={data} />}

      <div className="graph-toolbar">
        <button className="graph-btn" onClick={() => downloadHumanFactors("xlsx")}>
          Export XLSX
        </button>
        <button className="graph-btn" onClick={() => downloadHumanFactors("csv")}>
          Export CSV
        </button>
        <button className="graph-btn" onClick={() => downloadHumanFactors("json")}>
          Export JSON
        </button>
      </div>
    </div>
  );
};

export default GraphScreen;

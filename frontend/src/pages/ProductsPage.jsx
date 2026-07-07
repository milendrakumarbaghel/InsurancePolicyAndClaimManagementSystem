import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { Plus, Boxes, Pencil, Power, PowerOff, ArrowRight, Download } from "lucide-react";
import PageHeader from "../components/common/PageHeader";
import Card from "../components/common/Card";
import Button from "../components/common/Button";
import Spinner from "../components/common/Spinner";
import EmptyState from "../components/common/EmptyState";
import Stamp from "../components/common/Stamp";
import { useAuth } from "../context/AuthContext";
import { productService } from "../services/productService";
import { getErrorMessage } from "../services/api";
import { ROLES } from "../utils/constants";
import { toTitleCase } from "../utils/formatters";
import { exportToCSV } from "../utils/exportCsv";

export default function ProductsPage() {
  const { role } = useAuth();
  const isAdmin = role === ROLES.ADMIN;
  const navigate = useNavigate();

  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);

  const load = () => {
    setIsLoading(true);
    const request = (isAdmin || role === ROLES.AGENT)
      ? productService.getAll({ page: 0, size: 100 })
      : productService.getActive({ page: 0, size: 100 });
    request
      .then((data) => setProducts(data?.content ?? []))
      .catch((err) => toast.error(getErrorMessage(err, "Could not load products.")))
      .finally(() => setIsLoading(false));
  };

  useEffect(load, [isAdmin]);

  const toggleActive = async (product) => {
    setBusyId(product.productId);
    try {
      if (product.active) {
        await productService.deactivate(product.productId);
        toast.success(`${product.productName} deactivated.`);
      } else {
        await productService.activate(product.productId);
        toast.success(`${product.productName} activated.`);
      }
      load();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setBusyId(null);
    }
  };

  const handleExport = () => {
    if (!products || products.length === 0) {
      toast.error("No data to export.");
      return;
    }
    exportToCSV("products", products, [
      { key: "productId", header: "Product ID" },
      { key: "productName", header: "Product Name" },
      { key: "productType", header: "Type", format: (v) => toTitleCase(v) },
      { key: "description", header: "Description" },
      { key: "active", header: "Status", format: (v) => (v ? "Active" : "Inactive") },
    ]);
    toast.success("Products exported successfully.");
  };

  return (
    <div>
      <PageHeader
        eyebrow="Catalog"
        title={isAdmin ? "Insurance Products" : "Browse Products"}
        description={
          isAdmin
            ? "Create and manage the product lines your plans are built on."
            : "Explore available product lines, then view plans to purchase coverage."
        }
        actions={
          <>
            <Button icon={Download} variant="outline" onClick={handleExport}>Export CSV</Button>
            {isAdmin && (
              <Button icon={Plus} onClick={() => navigate("/dashboard/products/new")}>
                New product
              </Button>
            )}
          </>
        }
      />

      {isLoading ? (
        <Spinner label="Loading products…" />
      ) : products.length === 0 ? (
        <EmptyState icon={Boxes} title="No products yet" description="Once products are added, they'll show up here." />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {products.map((product) => (
            <Card key={product.productId} className="flex flex-col">
              <div className="flex items-start justify-between">
                <span className="rounded-lg bg-harbor-50 dark:bg-ink-800 px-2.5 py-1 text-xs font-semibold uppercase tracking-wider text-harbor-600">
                  {toTitleCase(product.productType)}
                </span>
                <Stamp status={product.active} />
              </div>
              <h3 className="mt-4 font-display text-lg font-semibold text-ink-900 dark:text-white">{product.productName}</h3>
              <p className="mt-1.5 text-sm text-ink-500 dark:text-ink-400 line-clamp-3 flex-1">{product.description}</p>

              <div className="mt-5 flex items-center gap-2">
                <Button
                  as={Link}
                  to={`/dashboard/products/${product.productId}/plans`}
                  size="sm"
                  icon={ArrowRight}
                  className="flex-row-reverse flex-1"
                >
                  View plans
                </Button>
                {isAdmin && (
                  <>
                    <Button
                      variant="outline"
                      size="sm"
                      icon={Pencil}
                      onClick={() => navigate(`/dashboard/products/${product.productId}/edit`)}
                    />
                    <Button
                      variant={product.active ? "danger" : "primary"}
                      size="sm"
                      icon={product.active ? PowerOff : Power}
                      isLoading={busyId === product.productId}
                      onClick={() => toggleActive(product)}
                    />
                  </>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

import { useNavigate } from "react-router-dom";
import { useState } from "react";
import PageHeader from "../components/common/PageHeader";
import DataTable from "../components/common/DataTable";
import Pagination from "../components/common/Pagination";
import Input from "../components/common/Input";
import Button from "../components/common/Button";
import { Search } from "lucide-react";
import { customerService } from "../services/customerService";
import { usePagedResource } from "../hooks/usePagedResource";
import { formatDate } from "../utils/formatters";

export default function CustomersPage() {
  const navigate = useNavigate();
  const [searchInput, setSearchInput] = useState("");
  const { content, page, setPage, totalPages, isLoading, setFilter } = usePagedResource(
    (params) => customerService.getAll(params),
    { size: 10, sortBy: "id", sortDir: "desc" }
  );

  const columns = [
    { key: "fullName", header: "Name", render: (r) => <span className="font-medium text-ink-900 dark:text-white">{r.fullName}</span> },
    { key: "email", header: "Email" },
    { key: "mobileNumber", header: "Mobile" },
    { key: "city", header: "City" },
    { key: "dateOfBirth", header: "Date of birth", render: (r) => formatDate(r.dateOfBirth) },
    {
      key: "actions",
      header: "",
      render: (r) => (
        <Button size="sm" variant="outline" onClick={() => navigate(`/dashboard/customers/${r.customerId}`)}>
          View
        </Button>
      ),
    },
  ];

  return (
    <div>
      <PageHeader eyebrow="Directory" title="Customers" description="Everyone who holds a policy with Assurly." />

      <form
        className="mb-4 flex gap-2 max-w-sm"
        onSubmit={(e) => {
          e.preventDefault();
          setFilter("search", searchInput);
        }}
      >
        <Input
          placeholder="Search by name or email"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          containerClassName="flex-1"
        />
        <Button type="submit" icon={Search} variant="outline" />
      </form>

      <DataTable columns={columns} data={content} isLoading={isLoading} keyField="customerId" emptyTitle="No customers found" />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}

import { useState } from "react";
import toast from "react-hot-toast";
import { Power, PowerOff } from "lucide-react";
import PageHeader from "../../components/common/PageHeader";
import DataTable from "../../components/common/DataTable";
import Pagination from "../../components/common/Pagination";
import Stamp from "../../components/common/Stamp";
import Button from "../../components/common/Button";
import { userService } from "../../services/userService";
import { getErrorMessage } from "../../services/api";
import { usePagedResource } from "../../hooks/usePagedResource";
import { toTitleCase } from "../../utils/formatters";

export default function UsersPage() {
  const [busyId, setBusyId] = useState(null);
  const { content, page, setPage, totalPages, isLoading, refresh } = usePagedResource(
    (params) => userService.getAll(params),
    { size: 10 }
  );

  const toggleActive = async (u) => {
    setBusyId(u.userId);
    try {
      if (u.active) {
        await userService.deactivate(u.userId);
        toast.success(`${u.fullName} deactivated.`);
      } else {
        await userService.activate(u.userId);
        toast.success(`${u.fullName} activated.`);
      }
      refresh();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setBusyId(null);
    }
  };

  const columns = [
    { key: "fullName", header: "Name", render: (r) => <span className="font-medium text-ink-900 dark:text-white">{r.fullName}</span> },
    { key: "email", header: "Email" },
    { key: "mobileNumber", header: "Mobile" },
    { key: "role", header: "Role", render: (r) => toTitleCase(r.role) },
    { key: "active", header: "Status", render: (r) => <Stamp status={r.active} /> },
    {
      key: "actions",
      header: "",
      render: (r) => (
        <Button
          size="sm"
          variant={r.active ? "danger" : "primary"}
          icon={r.active ? PowerOff : Power}
          isLoading={busyId === r.userId}
          onClick={() => toggleActive(r)}
        >
          {r.active ? "Deactivate" : "Activate"}
        </Button>
      ),
    },
  ];

  return (
    <div>
      <PageHeader eyebrow="Access" title="All Users" description="Every account in the system, across every role." />
      <DataTable columns={columns} data={content} isLoading={isLoading} keyField="userId" emptyTitle="No users found" />
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}

import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { UserPlus } from "lucide-react";
import PageHeader from "../../components/common/PageHeader";
import DataTable from "../../components/common/DataTable";
import Stamp from "../../components/common/Stamp";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { useForm } from "../../hooks/useForm";
import { userService } from "../../services/userService";
import { authService } from "../../services/authService";
import { getErrorMessage } from "../../services/api";
import { patterns, required, minLength, maxLength, pattern, email } from "../../utils/validators";

const schema = {
  fullName: [
    required("Full name is required"),
    minLength(3, "Must be between 3 and 100 characters"),
    maxLength(100, "Must be between 3 and 100 characters"),
    pattern(patterns.fullName, "Letters and spaces only"),
  ],
  email: [required("Email is required"), email("Invalid email format")],
  mobileNumber: [required("Mobile number is required"), pattern(patterns.mobileNumber, "10-digit number starting 6-9")],
  password: [
    required("Password is required"),
    minLength(8, "Must be between 8 and 20 characters"),
    maxLength(20, "Must be between 8 and 20 characters"),
    pattern(patterns.password, "Needs upper, lower, digit, special character"),
  ],
};

export default function AgentsPage() {
  const [agents, setAgents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError, reset } = useForm({
    initialValues: { fullName: "", email: "", mobileNumber: "", password: "" },
    schema,
    onSubmit: async (formValues) => {
      await authService.createAgent(formValues);
      toast.success("Agent account created.");
      setModalOpen(false);
      reset();
      load();
    },
  });

  const load = () => {
    setIsLoading(true);
    userService
      .getAgents()
      .then(setAgents)
      .catch((err) => toast.error(getErrorMessage(err, "Could not load agents.")))
      .finally(() => setIsLoading(false));
  };

  useEffect(load, []);

  const columns = [
    { key: "fullName", header: "Name", render: (r) => <span className="font-medium text-ink-900 dark:text-white">{r.fullName}</span> },
    { key: "email", header: "Email" },
    { key: "mobileNumber", header: "Mobile" },
    { key: "active", header: "Status", render: (r) => <Stamp status={r.active} /> },
  ];

  return (
    <div>
      <PageHeader
        eyebrow="Access"
        title="Agents"
        description="Agents review and recommend decisions on assigned claims."
        actions={<Button icon={UserPlus} onClick={() => setModalOpen(true)}>New agent</Button>}
      />

      {isLoading ? <Spinner label="Loading agents…" /> : <DataTable columns={columns} data={agents} keyField="userId" emptyTitle="No agents yet" />}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Create agent account"
        footer={
          <>
            <Button variant="outline" onClick={() => setModalOpen(false)}>Cancel</Button>
            <Button isLoading={isSubmitting} onClick={handleSubmit}>Create agent</Button>
          </>
        }
      >
        <form className="space-y-4" onSubmit={handleSubmit} noValidate>
          {submitError && <Alert type="error">{submitError}</Alert>}
          <Input label="Full name" name="fullName" value={values.fullName} onChange={handleChange} onBlur={handleBlur} error={errors.fullName} required />
          <Input label="Email" name="email" type="email" value={values.email} onChange={handleChange} onBlur={handleBlur} error={errors.email} required />
          <Input label="Mobile number" name="mobileNumber" value={values.mobileNumber} onChange={handleChange} onBlur={handleBlur} error={errors.mobileNumber} required />
          <Input label="Temporary password" name="password" type="password" value={values.password} onChange={handleChange} onBlur={handleBlur} error={errors.password} required />
        </form>
      </Modal>
    </div>
  );
}

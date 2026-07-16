import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { UserPlus, Download } from "lucide-react";
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
import { exportToCSV } from "../../utils/exportCsv";
import { getFullName } from "../../utils/formatters";

const schema = {
  firstName: [
    required("First name is required"),
    minLength(2, "Must be between 2 and 50 characters"),
    maxLength(50, "Must be between 2 and 50 characters"),
    pattern(patterns.nameField, "Letters only"),
  ],
  middleName: [
    maxLength(50, "Must not exceed 50 characters"),
    pattern(patterns.nameField, "Letters only"),
  ],
  lastName: [
    required("Last name is required"),
    minLength(2, "Must be between 2 and 50 characters"),
    maxLength(50, "Must be between 2 and 50 characters"),
    pattern(patterns.nameField, "Letters only"),
  ],
  email: [
    required("Email is required"),
    email("Invalid email format"),
    maxLength(255, "Must not exceed 255 characters"),
  ],
  mobileNumber: [required("Mobile number is required"), pattern(patterns.mobileNumber, "10-digit number starting 6-9")],
  password: [
    required("Password is required"),
    minLength(8, "Must be between 8 and 20 characters"),
    maxLength(20, "Must be between 8 and 20 characters"),
    pattern(patterns.password, "Needs upper, lower, digit, special character"),
  ],
};

export default function InsuranceOperationsOfficersPage() {
  const [insuranceOperationsOfficers, setInsuranceOperationsOfficers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);

  const { values, errors, handleChange, handleBlur, handleSubmit, isSubmitting, submitError, reset } = useForm({
    initialValues: { firstName: "", middleName: "", lastName: "", email: "", mobileNumber: "", password: "" },
    schema,
    onSubmit: async (formValues) => {
      await authService.createInsuranceOperationsOfficer(formValues);
      toast.success("Insurance operations officer account created.");
      setModalOpen(false);
      reset();
      load();
    },
  });

  const load = () => {
    setIsLoading(true);
    userService
      .getInsuranceOperationsOfficers()
      .then(setInsuranceOperationsOfficers)
      .catch((err) => toast.error(getErrorMessage(err, "Could not load insurance operations officers.")))
      .finally(() => setIsLoading(false));
  };

  useEffect(load, []);

  const handleExport = () => {
    if (!insuranceOperationsOfficers || insuranceOperationsOfficers.length === 0) {
      toast.error("No data to export.");
      return;
    }
    exportToCSV("insurance_operations_officers", insuranceOperationsOfficers, [
      { key: "userId", header: "User ID" },
      { key: "firstName", header: "First Name" },
      { key: "middleName", header: "Middle Name" },
      { key: "lastName", header: "Last Name" },
      { key: "email", header: "Email" },
      { key: "mobileNumber", header: "Mobile" },
      { key: "active", header: "Status", format: (v) => (v ? "Active" : "Inactive") },
    ]);
    toast.success("Insurance operations officers exported successfully.");
  };

  const columns = [
    { key: "firstName", header: "Name", render: (r) => <span className="font-medium text-ink-900 dark:text-white">{getFullName(r)}</span> },
    { key: "email", header: "Email" },
    { key: "mobileNumber", header: "Mobile" },
    { key: "active", header: "Status", render: (r) => <Stamp status={r.active} /> },
  ];

  return (
    <div>
      <PageHeader
        eyebrow="Access"
        title="Insurance Operations Officers"
        description="Insurance operations officers review and recommend decisions on assigned claims."
        actions={
          <>
            <Button icon={Download} variant="outline" onClick={handleExport}>Export CSV</Button>
            <Button icon={UserPlus} onClick={() => setModalOpen(true)}>New insurance operations officer</Button>
          </>
        }
      />

      {isLoading ? <Spinner label="Loading insurance operations officers..." /> : <DataTable columns={columns} data={insuranceOperationsOfficers} keyField="userId" emptyTitle="No insurance operations officers yet" />}

      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Create insurance operations officer account"
        footer={
          <>
            <Button variant="outline" onClick={() => setModalOpen(false)}>Cancel</Button>
            <Button isLoading={isSubmitting} onClick={handleSubmit}>Create insurance operations officer</Button>
          </>
        }
      >
        <form className="space-y-4" onSubmit={handleSubmit} noValidate>
          {submitError && <Alert type="error">{submitError}</Alert>}

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <Input label="First name" name="firstName" value={values.firstName} onChange={handleChange} onBlur={handleBlur} error={errors.firstName} required />
            <Input label="Middle name" name="middleName" value={values.middleName} onChange={handleChange} onBlur={handleBlur} error={errors.middleName} />
            <Input label="Last name" name="lastName" value={values.lastName} onChange={handleChange} onBlur={handleBlur} error={errors.lastName} required />
          </div>
          <Input label="Email" name="email" type="email" value={values.email} onChange={handleChange} onBlur={handleBlur} error={errors.email} required />
          <Input label="Mobile number" name="mobileNumber" value={values.mobileNumber} onChange={handleChange} onBlur={handleBlur} error={errors.mobileNumber} required />
          <Input label="Temporary password" name="password" type="password" value={values.password} onChange={handleChange} onBlur={handleBlur} error={errors.password} required />
        </form>
      </Modal>
    </div>
  );
}
import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { Save, UserCircle, Plus, Trash2 } from "lucide-react";
import Card from "../../components/common/Card";
import PageHeader from "../../components/common/PageHeader";
import Input from "../../components/common/Input";
import Select from "../../components/common/Select";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { useForm } from "../../hooks/useForm";
import { useAuth } from "../../context/AuthContext";
import { customerService } from "../../services/customerService";
import { getErrorMessage } from "../../services/api";
import {
  required,
  maxLength,
  pattern,
  patterns,
  pastDate18Years,
} from "../../utils/validators";
import { ALLOWED_RELATIONS } from "../../utils/constants";

const schema = {
  dateOfBirth: [required("Date of birth is required"), pastDate18Years()],
  address: [
    required("Address is required"),
    maxLength(150, "Cannot exceed 150 characters"),
  ],
  city: [
    required("City is required"),
    pattern(patterns.lettersSpaces, "Letters and spaces only"),
  ],
  state: [
    required("State is required"),
    pattern(patterns.lettersSpaces, "Letters and spaces only"),
  ],
  pinCode: [
    required("Pin code is required"),
    pattern(patterns.pinCode, "Enter a valid 6-digit pin code"),
  ],
};

export default function MyProfilePage() {
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(true);
  const [customerId, setCustomerId] = useState(null);
  const [isNewProfile, setIsNewProfile] = useState(false);

  const {
    values,
    errors,
    setValues,
    setFieldValue,
    handleChange,
    handleBlur,
    handleSubmit,
    isSubmitting,
    submitError,
  } = useForm({
    initialValues: {
      dateOfBirth: "",
      address: "",
      city: "",
      state: "",
      pinCode: "",
      nominees: [],
    },
    schema,
    onSubmit: async (formValues) => {
      // Form structure checks before dispatching payload definitions
      const invalidNominee = formValues.nominees.some(
        (n) => !n.name.trim() || !n.relation,
      );
      if (invalidNominee) {
        toast.error(
          "Please provide both name and relation fields for all listed nominees.",
        );
        return;
      }

      if (isNewProfile) {
        const created = await customerService.createProfile(formValues);
        setCustomerId(created.customerId);
        setIsNewProfile(false);
        toast.success("Profile created successfully.");
      } else {
        // Dispatches address alongside nominees nested inside a singular structured request context
        await customerService.updateProfile(customerId, formValues);
        toast.success("Profile details updated successfully.");
      }
    },
  });

  useEffect(() => {
    customerService
      .getMyProfile()
      .then((data) => {
        setCustomerId(data.customerId);
        setValues({
          dateOfBirth: data.dateOfBirth || "",
          address: data.address || "",
          city: data.city || "",
          state: data.state || "",
          pinCode: data.pinCode || "",
          nominees: data.nominees || [],
        });
      })
      .catch((err) => {
        if (err?.response?.status === 404) {
          setIsNewProfile(true);
        } else {
          toast.error(getErrorMessage(err, "Could not fetch profile trail."));
        }
      })
      .finally(() => setIsLoading(false));
  }, [setValues]);

  const handleNomineeChange = (index, field, value) => {
    const updated = [...values.nominees];
    updated[index][field] = value;
    setFieldValue("nominees", updated);
  };

  const addNomineeRow = () => {
    if (values.nominees.length >= 3) return;
    setFieldValue("nominees", [...values.nominees, { name: "", relation: "" }]);
  };

  const removeNomineeRow = (index) => {
    setFieldValue(
      "nominees",
      values.nominees.filter((_, i) => i !== index),
    );
  };

  if (isLoading) return <Spinner label="Loading profile workspace…" />;

  return (
    <div className="max-w-2xl">
      <PageHeader
        eyebrow="Account"
        title="My Profile"
        description={
          isNewProfile
            ? "Complete your KYC details to purchase policies and raise claims."
            : "Manage your address configuration and beneficiary alignments."
        }
      />

      <Card className="mb-6 flex items-center gap-4">
        <span className="flex h-12 w-12 items-center justify-center rounded-full bg-gold-500 text-ink-950">
          <UserCircle className="h-6 w-6" />
        </span>
        <div>
          <p className="font-medium text-ink-900 dark:text-white">
            {user?.name}
          </p>
          <p className="text-sm text-ink-500">{user?.email}</p>
        </div>
      </Card>

      <Card>
        {isNewProfile && (
          <Alert type="info" className="mb-5">
            Fill in your information below to register your account ledger
            profile.
          </Alert>
        )}
        <form onSubmit={handleSubmit} className="space-y-5" noValidate>
          {submitError && <Alert type="error">{submitError}</Alert>}

          {/* Dynamic Matrix Layout for Nominee Syncing */}
          <div className="border border-ink-200 dark:border-ink-800 rounded-xl p-4 bg-ink-50/20 dark:bg-ink-950/10">
            <div className="flex items-center justify-between mb-4">
              <label className="text-sm font-semibold text-ink-900 dark:text-white">
                Nominees ({values.nominees.length}/3)
              </label>
              {values.nominees.length < 3 && (
                <Button
                  type="button"
                  size="sm"
                  variant="outline"
                  icon={Plus}
                  onClick={addNomineeRow}
                >
                  Add Nominee
                </Button>
              )}
            </div>

            {values.nominees.length === 0 ? (
              <p className="text-xs text-ink-400 italic">
                No nominees assigned. Click Add Nominee to configure your
                beneficiaries (Maximum 3).
              </p>
            ) : (
              <div className="space-y-3">
                {values.nominees.map((nominee, index) => (
                  <div
                    key={index}
                    className="flex items-end gap-3 rounded-lg border border-ink-100 dark:border-ink-800 p-3 bg-white dark:bg-ink-900"
                  >
                    <Input
                      label="Nominee Name"
                      placeholder="Name"
                      value={nominee.name}
                      onChange={(e) =>
                        handleNomineeChange(index, "name", e.target.value)
                      }
                      containerClassName="flex-1"
                      required
                    />
                    <Select
                      label="Relation"
                      placeholder="Relationship"
                      options={ALLOWED_RELATIONS}
                      value={nominee.relation}
                      onChange={(e) =>
                        handleNomineeChange(index, "relation", e.target.value)
                      }
                      containerClassName="w-44"
                      required
                    />
                    <button
                      type="button"
                      onClick={() => removeNomineeRow(index)}
                      className="p-2.5 rounded-lg text-ink-400 hover:text-danger hover:bg-danger/5 transition-colors mb-0.5"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <Input
            label="Date of birth"
            name="dateOfBirth"
            type="date"
            max={new Date().toISOString().split("T")[0]}
            value={values.dateOfBirth}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.dateOfBirth}
            disabled={!isNewProfile} // Editable ONLY on baseline profile registration creation
            required
          />

          <Input
            label="Address"
            name="address"
            value={values.address}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.address}
            required
          />

          <div className="grid grid-cols-3 gap-4">
            <Input
              label="City"
              name="city"
              value={values.city}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.city}
              required
            />
            <Input
              label="State"
              name="state"
              value={values.state}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.state}
              required
            />
            <Input
              label="Pin code"
              name="pinCode"
              value={values.pinCode}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.pinCode}
              required
            />
          </div>

          <Button type="submit" isLoading={isSubmitting} icon={Save}>
            {isNewProfile ? "Create profile" : "Save changes"}
          </Button>
        </form>
      </Card>
    </div>
  );
}

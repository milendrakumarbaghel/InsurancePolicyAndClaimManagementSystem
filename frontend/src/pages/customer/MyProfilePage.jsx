import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import { Save, UserCircle } from "lucide-react";
import Card from "../../components/common/Card";
import PageHeader from "../../components/common/PageHeader";
import Input from "../../components/common/Input";
import Button from "../../components/common/Button";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { useForm } from "../../hooks/useForm";
import { useAuth } from "../../context/AuthContext";
import { customerService } from "../../services/customerService";
import { getErrorMessage } from "../../services/api";
import { patterns, required, pattern, pastDate18Years, maxLength } from "../../utils/validators";

const schema = {
  nomineeName: [required("Nominee name is required"), pattern(patterns.lettersSpaces, "Letters and spaces only")],
  nomineeRelation: [required("Nominee relation is required"), pattern(patterns.lettersSpaces, "Letters and spaces only")],
  dateOfBirth: [required("Date of birth is required"), pastDate18Years()],
  address: [required("Address is required"), maxLength(150, "Cannot exceed 150 characters")],
  city: [required("City is required"), pattern(patterns.lettersSpaces, "Letters and spaces only")],
  state: [required("State is required"), pattern(patterns.lettersSpaces, "Letters and spaces only")],
  pinCode: [required("Pin code is required"), pattern(patterns.pinCode, "Enter a valid 6-digit pin code")],
};

export default function MyProfilePage() {
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(true);
  const [customerId, setCustomerId] = useState(null);
  const [isNewProfile, setIsNewProfile] = useState(false);

  const { values, errors, setValues, handleChange, handleBlur, handleSubmit, isSubmitting, submitError } = useForm({
    initialValues: {
      nomineeName: "", nomineeRelation: "", dateOfBirth: "", address: "", city: "", state: "", pinCode: "",
    },
    schema,
    onSubmit: async (formValues) => {
      if (isNewProfile) {
        const created = await customerService.createProfile(formValues);
        setCustomerId(created.customerId);
        setIsNewProfile(false);
        toast.success("Profile created.");
      } else {
        await customerService.updateProfile(customerId, formValues);
        toast.success("Profile updated.");
      }
    },
  });

  useEffect(() => {
    customerService
      .getMyProfile()
      .then((data) => {
        setCustomerId(data.customerId);
        setValues({
          nomineeName: data.nomineeName || "",
          nomineeRelation: data.nomineeRelation || "",
          dateOfBirth: data.dateOfBirth || "",
          address: data.address || "",
          city: data.city || "",
          state: data.state || "",
          pinCode: data.pinCode || "",
        });
      })
      .catch((err) => {
        if (err?.response?.status === 404) {
          setIsNewProfile(true);
        } else {
          toast.error(getErrorMessage(err, "Could not load your profile."));
        }
      })
      .finally(() => setIsLoading(false));
  }, [setValues]);

  if (isLoading) return <Spinner label="Loading your profile…" />;

  return (
    <div className="max-w-2xl">
      <PageHeader
        eyebrow="Account"
        title="My Profile"
        description={isNewProfile ? "Complete your KYC details to purchase policies and raise claims." : "Keep your nominee and address details up to date."}
      />

      <Card className="mb-6 flex items-center gap-4">
        <span className="flex h-12 w-12 items-center justify-center rounded-full bg-gold-500 text-ink-950">
          <UserCircle className="h-6 w-6" />
        </span>
        <div>
          <p className="font-medium text-ink-900 dark:text-white">{user?.name}</p>
          <p className="text-sm text-ink-500">{user?.email}</p>
        </div>
      </Card>

      <Card>
        {isNewProfile && <Alert type="info" className="mb-5">You haven't set up your customer profile yet — fill this in to get started.</Alert>}
        <form onSubmit={handleSubmit} className="space-y-5" noValidate>
          {submitError && <Alert type="error">{submitError}</Alert>}

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Nominee name"
              name="nomineeName"
              value={values.nomineeName}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.nomineeName}
              required
            />
            <Input
              label="Nominee relation"
              name="nomineeRelation"
              placeholder="Spouse, Parent, Sibling…"
              value={values.nomineeRelation}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.nomineeRelation}
              required
            />
          </div>

          <Input
            label="Date of birth"
            name="dateOfBirth"
            type="date"
            value={values.dateOfBirth}
            onChange={handleChange}
            onBlur={handleBlur}
            error={errors.dateOfBirth}
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
            <Input label="City" name="city" value={values.city} onChange={handleChange} onBlur={handleBlur} error={errors.city} required />
            <Input label="State" name="state" value={values.state} onChange={handleChange} onBlur={handleBlur} error={errors.state} required />
            <Input label="Pin code" name="pinCode" value={values.pinCode} onChange={handleChange} onBlur={handleBlur} error={errors.pinCode} required />
          </div>

          <Button type="submit" isLoading={isSubmitting} icon={Save}>
            {isNewProfile ? "Create profile" : "Save changes"}
          </Button>
        </form>
      </Card>
    </div>
  );
}

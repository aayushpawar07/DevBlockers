import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { blockerService } from '../../services/blockerService';
import { userService } from '../../services/userService';
import { useAuth } from '../../context/AuthContext';
import { Card, CardBody, CardHeader } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input, Textarea, Select } from '../../components/ui/Input';
import { BLOCKER_SEVERITY } from '../../utils/constants';
import { X, Upload, Image as ImageIcon, Video } from 'lucide-react';
import toast from 'react-hot-toast';

const TEAM_CODES = [
  { value: 'DEVOPS', label: 'DevOps' },
  { value: 'BACKEND', label: 'Backend' },
  { value: 'FRONTEND', label: 'Frontend' },
  { value: 'QA', label: 'QA' },
];

export const CreateBlocker = () => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    severity: 'MEDIUM',
    teamCode: '',
    tags: '',
  });
  const [userTeams, setUserTeams] = useState([]);
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [uploadedFileUrls, setUploadedFileUrls] = useState([]);
  const [uploadingFiles, setUploadingFiles] = useState(false);
  const [loading, setLoading] = useState(false);
  const [loadingTeams, setLoadingTeams] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (user?.userId) {
      fetchUserTeams();
    }
  }, [user?.userId]);

  const fetchUserTeams = async () => {
    try {
      setLoadingTeams(true);
      const teams = await userService.getUserTeams(user.userId);
      setUserTeams(teams || []);
    } catch (error) {
      console.error('Failed to fetch user teams:', error);
      // Don't show error toast - user can still create blocker
    } finally {
      setLoadingTeams(false);
    }
  };

  const handleFileSelect = (e) => {
    const files = Array.from(e.target.files);
    setSelectedFiles((prev) => [...prev, ...files]);
  };

  const handleRemoveFile = (index) => {
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleUploadFiles = async () => {
    if (selectedFiles.length === 0) return;

    setUploadingFiles(true);
    try {
      const response = await blockerService.uploadFiles(selectedFiles);
      setUploadedFileUrls((prev) => [...prev, ...response.fileUrls]);
      setSelectedFiles([]);
      toast.success('Files uploaded successfully!');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to upload files');
      console.error(error);
    } finally {
      setUploadingFiles(false);
    }
  };

  const handleRemoveUploadedFile = (index) => {
    setUploadedFileUrls((prev) => prev.filter((_, i) => i !== index));
  };

  const isImage = (url) => {
    return /\.(jpg|jpeg|png|gif|webp)$/i.test(url);
  };

  const isVideo = (url) => {
    return /\.(mp4|webm|mov|avi)$/i.test(url);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!user?.userId) {
      toast.error('User not authenticated');
      return;
    }

    if (!formData.teamCode) {
      toast.error('Please select a team');
      return;
    }

    setLoading(true);
    try {
      console.log('Creating blocker with mediaUrls:', uploadedFileUrls);
      const blocker = await blockerService.createBlocker({
        ...formData,
        createdBy: user.userId,
        tags: formData.tags.split(',').map((tag) => tag.trim()).filter(Boolean),
        mediaUrls: uploadedFileUrls,
      });
      console.log('Blocker created:', blocker);
      console.log('Created blocker mediaUrls:', blocker.mediaUrls);
      toast.success('Blocker created successfully!');
      navigate(`/blockers/${blocker.blockerId}`);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create blocker');
      console.error('Error creating blocker:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto">
      <Card>
        <CardHeader>
          <h1 className="text-3xl font-bold text-gray-900">Create New Blocker</h1>
          <p className="mt-2 text-sm text-gray-600">
            Describe the issue you're facing in detail
          </p>
        </CardHeader>
        <CardBody>
          <form onSubmit={handleSubmit} className="space-y-6">
            <Input
              label="Title"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
              placeholder="Brief description of the blocker"
            />
            <Textarea
              label="Description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              required
              rows={8}
              placeholder="Provide detailed information about the blocker, steps to reproduce, expected vs actual behavior..."
            />
            <Select
              label="Severity"
              value={formData.severity}
              onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
              options={Object.values(BLOCKER_SEVERITY).map((severity) => ({
                value: severity,
                label: severity,
              }))}
            />
            <Select
              label="Team *"
              value={formData.teamCode}
              onChange={(e) => setFormData({ ...formData, teamCode: e.target.value })}
              options={TEAM_CODES}
              required
              disabled={loadingTeams}
            />
            <Input
              label="Tags (comma-separated)"
              value={formData.tags}
              onChange={(e) => setFormData({ ...formData, tags: e.target.value })}
              placeholder="e.g., bug, frontend, api"
            />
            
            {/* File Upload Section */}
            <div className="space-y-4">
              <label className="block text-sm font-medium text-gray-700">
                Photos & Videos (Optional)
              </label>
              
              {/* File Input */}
              <div className="flex items-center gap-4">
                <label className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors">
                  <Upload className="w-5 h-5" />
                  <span className="text-sm font-medium">Choose Files</span>
                  <input
                    type="file"
                    multiple
                    accept="image/*,video/*"
                    onChange={handleFileSelect}
                    className="hidden"
                  />
                </label>
                {selectedFiles.length > 0 && (
                  <Button
                    type="button"
                    onClick={handleUploadFiles}
                    loading={uploadingFiles}
                    variant="secondary"
                  >
                    Upload {selectedFiles.length} file(s)
                  </Button>
                )}
              </div>

              {/* Selected Files Preview */}
              {selectedFiles.length > 0 && (
                <div className="flex flex-wrap gap-2">
                  {selectedFiles.map((file, index) => (
                    <div
                      key={index}
                      className="flex items-center gap-2 px-3 py-1 bg-gray-100 rounded-lg text-sm"
                    >
                      <span className="truncate max-w-[200px]">{file.name}</span>
                      <button
                        type="button"
                        onClick={() => handleRemoveFile(index)}
                        className="text-red-600 hover:text-red-800"
                      >
                        <X className="w-4 h-4" />
                      </button>
                    </div>
                  ))}
                </div>
              )}

              {/* Uploaded Files Preview */}
              {uploadedFileUrls.length > 0 && (
                <div className="space-y-2">
                  <p className="text-sm text-gray-600">Uploaded files:</p>
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                    {uploadedFileUrls.map((url, index) => {
                      const fullUrl = url.startsWith('http') ? url : blockerService.getFileUrl(url.split('/').pop());
                      return (
                        <div key={index} className="relative group">
                          {isImage(url) ? (
                            <img
                              src={fullUrl}
                              alt={`Upload ${index + 1}`}
                              className="w-full h-32 object-cover rounded-lg border border-gray-200"
                            />
                          ) : isVideo(url) ? (
                            <video
                              src={fullUrl}
                              className="w-full h-32 object-cover rounded-lg border border-gray-200"
                              controls
                            />
                          ) : (
                            <div className="w-full h-32 bg-gray-100 rounded-lg border border-gray-200 flex items-center justify-center">
                              <ImageIcon className="w-8 h-8 text-gray-400" />
                            </div>
                          )}
                          <button
                            type="button"
                            onClick={() => handleRemoveUploadedFile(index)}
                            className="absolute top-2 right-2 bg-red-600 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                          >
                            <X className="w-4 h-4" />
                          </button>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
            </div>

            <div className="flex gap-4">
              <Button type="submit" className="flex-1" loading={loading}>
                Create Blocker
              </Button>
              <Button
                type="button"
                variant="secondary"
                onClick={() => navigate('/blockers')}
              >
                Cancel
              </Button>
            </div>
          </form>
        </CardBody>
      </Card>
    </div>
  );
};

